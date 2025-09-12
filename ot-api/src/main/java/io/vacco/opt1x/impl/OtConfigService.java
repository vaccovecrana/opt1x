package io.vacco.opt1x.impl;

import com.google.gson.Gson;
import io.vacco.murmux.http.MxMime;
import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import io.vacco.ronove.RvResponse;
import jakarta.ws.rs.core.Response;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.opt1x.schema.OtConfig.config;
import static io.vacco.opt1x.dto.OtConfigOp.configOp;
import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class OtConfigService {

  public final OtDaos daos;
  public final OtValueService valService;
  public final OtAdminService admService;
  public final OtSealService sealService;
  public final Gson json;

  public OtConfigService(OtDaos daos,
                         OtValueService valService,
                         OtAdminService admService,
                         OtSealService sealService, Gson json) {
    this.daos = requireNonNull(daos);
    this.valService = requireNonNull(valService);
    this.admService = requireNonNull(admService);
    this.sealService = requireNonNull(sealService);
    this.json = requireNonNull(json);
  }

  public OtConfigOp duplicateConfig(OtConfigOp cmd) {
    var configs = daos.cfd.loadWhereNsIdEq(cmd.cfg.nsId);
    for (var cfg : configs) {
      if (cfg.name.equals(cmd.cfg.name)) {
        cmd = cmd
          .withConfig(cfg)
          .withError(format("Config [%s] already exists in namespace [%s]", cmd.cfg.name, cmd.cfg.nsId));
        break;
      }
    }
    return cmd;
  }

  public OtConfigOp createConfig(OtConfigOp cmd) {
    try {
      cmd.clear();
      cmd = requireNonNull(cmd).validate(cmd0 -> {
        var kg = admService.canAccessNs(cmd0.key.kid, cmd0.cfg.nsId, false, true, false);
        if (kg.isEmpty()) {
          return admService.noNsAccess(cmd0, cmd0.key.kid, cmd0.cfg.nsId, write);
        }
        return cmd0;
      });
      cmd.cfg.createUtcMs = System.currentTimeMillis();
      cmd = OtValid
        .validate(cmd.cfg, cmd)
        .validate(this::duplicateConfig);
      if (cmd.ok()) {
        daos.cfd.save(cmd.cfg);
      }
      return cmd;
    } catch (Exception e) {
      onError("Config create error", e);
      return cmd.withError(e);
    }
  }

  public OtResult nodeData(OtResult cmd, OtNode node) {
    var cfg = daos.cfd.load(node.cid);
    if (cfg.isEmpty()) {
      return cmd.withError(format("Config [%d] does not exist", node.cid));
    }
    if (node.type == OtNodeType.Value) {
      var val = daos.vld.load(node.vid);
      if (val.isEmpty()) {
        return cmd.withError(format("Config value [%d] does not exist", node.vid));
      }
    }
    return cmd;
  }

  public Map<String, OtVar> treeStructure(OtConfigOp cmd) {
    var root = cmd.vars.stream().filter(v -> v.node.pNid == null).findFirst();
    if (root.isPresent()) {
      if (root.get().node.nid == null) {
        root.get().node.nid = -1;
      }
      var idIdx = new LinkedHashMap<Integer, Integer>();
      var dbIdx = new LinkedHashMap<Integer, OtVar>();
      var ptIdx = new LinkedHashMap<String, OtVar>();
      for (var v : cmd.vars) {
        var nid0 = v.node.nid;
        if (nid0 == null) {
          cmd.withError(format("Config node [%s] has no id", v.node.label));
        }
        daos.ndd.idOf(v.node).ifPresent(id -> v.node.nid = id);
        idIdx.put(nid0, v.node.nid);
        dbIdx.put(v.node.nid, v);
      }
      for (var v : cmd.vars) {
        if (v.node.pNid == null) {
          var rnId = root.get().node.nid;
          if (rnId != null && !rnId.equals(v.node.nid)) {
            cmd.withError("Config tree must have only one root node");
            return Collections.emptyMap();
          }
        }
        var path = new ArrayList<Integer>();
        var pv = v;
        while (pv != null) {
          path.add(pv.node.nid);
          pv = dbIdx.get(idIdx.get(pv.node.pNid));
          if (pv != null && pv.node.nid.equals(v.node.nid)) {
            cmd.withError("Config node [%d] has invalid parent");
            return Collections.emptyMap();
          }
        }
        var vPath = path.reversed().stream()
          .map(Object::toString)
          .collect(Collectors.joining("/"));
        ptIdx.put(vPath, v);
      }
      for (var v : cmd.vars) {
        var pn = dbIdx.get(idIdx.get(v.node.pNid));
        if (pn != null) {
          v.node.pNid = pn.node.nid;
        }
      }
      return ptIdx;
    } else {
      cmd.withError("Config tree root node missing");
      return Collections.emptyMap();
    }
  }

  public OtConfigOp writeTree(OtConfigOp cmd, Map<String, OtVar> treeIdx) {
    try {
      daos.onTxResult(cmd, daos.ndd.sql().tx((tx, conn) -> {
        daos.ndd.deleteWhereCidEq(cmd.cfg.cid);
        for (var e : treeIdx.entrySet()) {
          daos.ndd.save(e.getValue().node);
        }
      }));
      return cmd;
    } catch (Exception e) {
      onError("Config tree write error", e);
      return cmd.withError(e);
    }
  }

  public OtConfigOp update(OtConfigOp cmd) {
    try {
      cmd.clear();
      cmd.cfg = daos.cfd.loadExisting(cmd.cfg.cid);
      var kg = admService.canAccessNs(cmd.key.kid, cmd.cfg.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, cmd.cfg.nsId, write);
      }
      for (var otv : requireNonNull(requireNonNull(cmd).vars)) {
        if (!OtValid.validate(otv.node, cmd).ok()) {
          return cmd;
        }
        if (otv.val != null && !OtValid.validate(otv.val, cmd).ok()) {
          return cmd;
        }
        if (!cmd.validate(cmd0 -> nodeData(cmd0, otv.node)).ok()) {
          return cmd;
        }
      }
      var treeIdx = treeStructure(cmd);
      if (!cmd.ok()) {
        return cmd;
      }
      return writeTree(cmd, treeIdx);
    } catch (Exception e) {
      onError("Config update error", e);
      return cmd.withError(e);
    }
  }

  public OtConfigOp load(OtConfigOp cmd) {
    try {
      cmd.cfg = daos.cfd.loadExisting(cmd.cfg.cid);
      var kg = admService.canAccessNs(cmd.key.kid, cmd.cfg.nsId, true, false, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, cmd.cfg.nsId, read);
      }
      if (cmd.ok()) {
        var nodes = daos.ndd.loadWhereCidEq(cmd.cfg.cid);
        var valIds = nodes.stream().map(node -> node.vid).filter(Objects::nonNull).toArray(Integer[]::new);
        var values = daos.vld.loadWhereVidIn(valIds);
        cmd.cfg = daos.cfd.loadExisting(cmd.cfg.cid);
        cmd.vars = nodes.stream().map(node -> {
          var value = (OtValue) null;
          var vl = values.get(node.vid);
          if (vl != null) {
            value = vl.getFirst();
            if (value.encrypted && !cmd.encrypted) {
              value = sealService.decrypt(value);
            }
          }
          return OtVar.of(node, value);
        }).collect(Collectors.toList());
      }
      return cmd;
    } catch (Exception e) {
      onError("Config load error", e);
      return cmd.withError(e);
    }
  }

  public OtConfigOp clone(OtConfigOp cmd) {
    try {
      var clone = config(cmd.cfg.cid, cmd.cfg.nsId, cmd.cfg.name);
      var kg = admService.canAccessNs(cmd.key.kid, cmd.cfg.nsId, true, false, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, cmd.cfg.nsId, read);
      }
      kg = admService.canAccessNs(cmd.key.kid, clone.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, clone.nsId, write);
      }
      daos.onTxResult(cmd, daos.cfd.sql().tx((tx, conn) -> {
        var nodes = daos.ndd.loadWhereCidEq(clone.cid);
        if (nodes.isEmpty()) {
          tx.rollback();
          cmd.withError("Config clone - no config nodes were found");
          return;
        }
        var createCmd = createConfig(configOp().withApiKey(cmd.key).withConfig(clone));
        if (!createCmd.ok()) {
          tx.rollback();
          cmd.setFrom(createCmd);
          return;
        }
        var idIdx = new HashMap<Integer, Integer>();
        nodes.sort(Comparator.comparingInt(n -> n.itemIdx));
        for (var node : nodes) {
          var oldId = node.nid;
          node.cid = createCmd.cfg.cid;
          daos.ndd.idOf(node).orElseThrow();
          idIdx.put(oldId, node.nid);
        }
        for (var node : nodes) {
          if (node.pNid != null) {
            node.pNid = idIdx.get(node.pNid);
          }
          daos.ndd.save(node);
        }
      }));
      return cmd;
    } catch (Exception e) {
      onError("Config clone error", e);
      return cmd.withError(e);
    }
  }

  public OtList<OtConfig, String> configsOf(Integer kid, Integer nsId, int pageSize, String next) {
    var out = new OtList<OtConfig, String>();
    try {
      var kg = admService.canAccessNs(kid, nsId, true, false, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(out, kid, nsId, read);
      }
      out.page = daos.cfd.loadPage1(
        daos.cfd.query().eq(daos.cfd.fld_nsId(), nsId).limit(pageSize),
        OtConfigDao.fld_name, next
      );
      return out;
    } catch (Exception e) {
      onError("Config list error", e);
      return out.withError(e);
    }
  }

  private <T> RvResponse<T> error(RvResponse<T> out, Response.Status status, T error) {
    return out
      .withStatus(status)
      .withMediaType("text/plain")
      .withBody(error);
  }

  private <T> RvResponse<T> ok(RvResponse<T> out, String mediaType, T body) {
    return out
      .withStatus(Response.Status.OK)
      .withMediaType(mediaType)
      .withBody(body);
  }

  public RvResponse<Object> render(OtConfigOp cmd, OtNodeFormat fmt) {
    var out = new RvResponse<>();
    var raw = OtRender.toMap(cmd.vars);
    switch (fmt) {
      case json: return ok(out, MxMime.json.type, raw);
      case yaml: return ok(out, "application/yaml", OtYaml.toYaml(raw));
      case toml: return ok(out, "application/toml", OtToml.toToml(raw));
      default:   return ok(out, "text/x-java-properties", OtProperties.toProperties(raw));
    }
  }

  public RvResponse<Object> render(OtApiKey key, Integer cid, String otFormat, boolean encrypted) {
    try {
      var cmd = configOp();
      var fmt = OtNodeFormat.valueOf(otFormat);
      var cfg = daos.cfd.loadExisting(cid);
      var kg = admService.canAccessNs(key.kid, cfg.nsId, true, false, false);
      if (kg.isEmpty()) {
        cmd = admService.noNsAccess(cmd, key.kid, cfg.nsId, read);
        return error(new RvResponse<>(), Response.Status.UNAUTHORIZED, cmd.error);
      }
      cmd.cfg = cfg;
      cmd.encrypted = encrypted;
      cmd.key = key;
      if (!load(cmd).ok()) {
        return error(new RvResponse<>(), Response.Status.BAD_REQUEST, cmd.error);
      }
      return render(cmd, fmt);
    } catch (Exception e) {
      return error(new RvResponse<>(), Response.Status.BAD_REQUEST, e.getMessage());
    }
  }

}
