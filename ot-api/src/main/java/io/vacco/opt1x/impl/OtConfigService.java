package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.OtConfigDao;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;
import java.util.stream.Collectors;

import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.lang.String.format;
import static java.util.Objects.requireNonNull;

public class OtConfigService {

  public final OtDaos daos;
  public final OtValueService valService;
  public final OtNamespaceService nsService;
  public final OtSealService sealService;

  public OtConfigService(OtDaos daos, OtValueService valService,
                         OtNamespaceService nsService, OtSealService sealService) {
    this.daos = requireNonNull(daos);
    this.valService = requireNonNull(valService);
    this.nsService = requireNonNull(nsService);
    this.sealService = requireNonNull(sealService);
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
      cmd = OtValid
        .validate(requireNonNull(cmd).cfg, cmd)
        .validate(this::duplicateConfig)
        .validate(cmd0 -> nsService.nsAccess(cmd0, cmd0.key.kid, cmd0.cfg.nsId, true));
      if (cmd.ok()) {
        cmd.cfg.createdAtUtcMs = System.currentTimeMillis();
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
        if (v.node.pNid == null && root.get().node.nid != null && !root.get().node.nid.equals(v.node.nid)) {
          cmd.withError("Config tree must have only one root node");
          return Collections.emptyMap();
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
      daos.ndd.sql().tx((tx, conn) -> {
        daos.ndd.deleteWhereCidEq(cmd.cfg.cid);
        for (var e : treeIdx.entrySet()) {
          daos.ndd.save(e.getValue().node);
        }
      });
      return cmd;
    } catch (Exception e) {
      onError("Config tree write error", e);
      return cmd.withError(e);
    }
  }

  public OtConfigOp update(OtConfigOp cmd) {
    try {
      final var key = cmd.key;
      final var nsId = cmd.cfg.nsId;
      if (!cmd.validate(cmd0 -> nsService.nsAccess(cmd0, key.kid, nsId, true)).ok()) {
        return cmd;
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
      var encrypted = cmd.encrypted;
      cmd = cmd.validate(cmd0 -> nsService.nsAccess(cmd0, cmd0.key.kid, cmd0.cfg.nsId, false));
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
            if (value.encrypted && !encrypted) {
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

  public OtList<OtConfig, String> configsOf(Integer kid, Integer nsId, int pageSize, String next) {
    var out = new OtList<OtConfig, String>();
    try {
      out = nsService.nsAccess(out, kid, nsId, false);
      if (!out.ok()) {
        return out;
      }
      out.page = daos.cfd.loadPage1(
        pageSize, false,
        daos.cfd.query().eq(daos.cfd.fld_nsId(), nsId),
        OtConfigDao.fld_name, next
      );
      return out;
    } catch (Exception e) {
      onError("Config list error", e);
      return out.withError(e);
    }
  }

}
