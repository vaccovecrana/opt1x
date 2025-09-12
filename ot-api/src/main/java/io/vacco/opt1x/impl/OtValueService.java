package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;

import static io.vacco.opt1x.schema.OtValueVer.version;
import static io.vacco.opt1x.dto.OtValidation.vld;
import static io.vacco.opt1x.schema.OtValue.value;
import static io.vacco.opt1x.dto.OtValueOp.valueOp;
import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class OtValueService {

  public  final OtDaos daos;
  private final OtAdminService admService;
  private final OtSealService sealService;

  public OtValueService(OtDaos daos,
                        OtAdminService admService,
                        OtSealService sealService) {
    this.daos = Objects.requireNonNull(daos);
    this.admService = Objects.requireNonNull(admService);
    this.sealService = Objects.requireNonNull(sealService);
  }

  private boolean isNumber(String raw) {
    try {
      Double.parseDouble(raw);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  private boolean isBoolean(String raw) {
    try {
      Boolean.parseBoolean(raw);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  public OtValueOp valueType(OtValueOp cmd) {
    if (cmd.val.type == OtValueType.String) {
      return cmd;
    }
    if (cmd.val.type == OtValueType.Number && !isNumber(cmd.val.val)) {
      return cmd.withError(format("Value [%s] is not a number", cmd.val.val));
    }
    if (cmd.val.type == OtValueType.Boolean && !isBoolean(cmd.val.val)) {
      return cmd.withError(format("Value [%s] is not a boolean", cmd.val.val));
    }
    return cmd;
  }

  public OtValueOp upsertValue(OtValueOp cmd) {
    try {
      cmd.clear();
      var kg = admService.canAccessNs(Objects.requireNonNull(cmd).key.kid, cmd.val.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, cmd.val.nsId, write);
      }
      cmd.val.createUtcMs = System.currentTimeMillis();
      cmd = OtValid
        .validate(Objects.requireNonNull(cmd).val, cmd)
        .validate(this::valueType);
      if (cmd.ok()) {
        if (cmd.val.encrypted) {
          cmd.val = sealService.encrypt(cmd.val);
        }
        final var v = cmd.val;
        daos.onTxResult(cmd, daos.vld.sql().tx((tx, conn) -> {
          if (v.vid != null) {
            var ovv = daos.vld.load(v.vid);
            ovv.ifPresent(v0 -> daos.vvd.upsert(version(v0, System.currentTimeMillis())));
          }
          daos.vld.upsert(v);
        }));
        cmd.valVersions = daos.vvd.loadWhereVidEq(cmd.val.vid);
      }
      return cmd;
    } catch (Exception e) {
      onError("Value create error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp versionsOf(OtApiKey key, Integer vid) {
    var cmd = valueOp();
    try {
      var v0 = daos.vld.loadExisting(vid);
      var kg = admService.canAccessNs(key.kid, v0.nsId, true, false, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, v0.nsId, write);
      }
      cmd.val = v0;
      cmd.valVersions = daos.vvd.loadWhereVidEq(vid);
      cmd.namespace = daos.nsd.loadExisting(v0.nsId);
      return cmd;
    } catch (Exception e) {
      onError("Value versions list error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp restoreValueVersion(OtApiKey key, Integer vvId) {
    var cmd = valueOp();
    try {
      var vv = daos.vvd.loadExisting(vvId);
      var v0 = daos.vld.loadExisting(vv.vid);
      var kg = admService.canAccessNs(key.kid, v0.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, v0.nsId, write);
      }
      daos.onTxResult(cmd, daos.vvd.sql().tx((tx, conn) -> {
        var vv0 = version(v0, System.currentTimeMillis());
        var v1 = value(v0.nsId, v0.name, vv.val, vv.type, vv.notes, v0.encrypted);
        v1.createUtcMs = System.currentTimeMillis();
        daos.vvd.upsert(vv0);
        daos.vld.upsert(v1);
        cmd.val = v1;
      }));
      cmd.valVersions = daos.vvd.loadWhereVidEq(v0.vid);
      cmd.namespace = daos.nsd.loadExisting(v0.nsId);
      return cmd;
    } catch (Exception e) {
      onError("Value version restore error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp deleteValueVersion(OtApiKey key, Integer vvId) {
    var cmd = valueOp();
    try {
      var vv = daos.vvd.loadExisting(vvId);
      var v0 = daos.vld.loadExisting(vv.vid);
      var kg = admService.canAccessNs(key.kid, v0.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, v0.nsId, write);
      }
      daos.vvd.deleteWhereIdEq(vvId);
      cmd.val = v0;
      cmd.valVersions = daos.vvd.loadWhereVidEq(v0.vid);
      cmd.namespace = daos.nsd.loadExisting(v0.nsId);
      return cmd;
    } catch (Exception e) {
      onError("Value version delete error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp accessibleValuesFor(OtApiKey key) {
    var cmd = valueOp();
    try {
      var nsd = daos.nsd;
      var nsl = new ArrayList<OtNamespace>();
      for (var ns : admService.accessNamespacesOf(key.kid).namespaces) {
        nsl.add(ns);
        var subNs = nsd.loadPageItems(nsd.query().like(nsd.fld_path(), daos.likeFmt(ns.path)));
        for (var sns : subNs) {
          if (!nsl.contains(sns)) {
            nsl.add(sns);
          }
        }
      }
      var nsIds = nsl.stream().map(ns -> ns.nsId).toArray(Integer[]::new);
      var values = daos.vld.listWhereNsIdIn(nsIds);
      cmd.namespaces = nsl;
      cmd.values = values;
      return cmd;
    } catch (Exception e) {
      onError("Value user access error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp valuesOf(Integer kid, Integer nsId, int pageSize, String next) {
    var out = valueOp();
    try {
      var kg = admService.canAccessNs(kid, nsId, true, false, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(out, kid, nsId, read);
      }
      var nsIdFld = daos.vld.fld_nsId();
      out.valPage = daos.vld.loadPage1(
        daos.vld.query().eq(nsIdFld, nsId).limit(pageSize),
        OtValueDao.fld_name, next
      );
      out.namespace = daos.nsd.loadExisting(nsId);
      return out;
    } catch (Exception e) {
      onError("Value list error", e);
      return out.withError(e);
    }
  }

  public OtValueOp deleteValue(OtApiKey key, Integer vid) {
    var cmd = valueOp();
    try {
      var v0 = daos.vld.loadExisting(vid);
      var kg = admService.canAccessNs(key.kid, v0.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, key.kid, v0.nsId, write);
      }
      var configs = daos.cfd.loadPageItems(
        daos.cfd.query()
          .from(daos.vld.dsc)
          .innerJoin(daos.ndd.dsc, daos.vld.dsc)
          .innerJoin(daos.cfd.dsc, daos.ndd.dsc)
          .eq(daos.vld.fld_vid(), vid)
      );
      if (!configs.isEmpty()) {
        cmd.validations.addAll(
          configs.stream()
            .map(cfg -> vld(null, cfg.name, null, null))
            .collect(toList())
        );
        return cmd.withError(format("Value delete - value [%d] has active configuration references", vid));
      }
      cmd.withVal(value(v0.nsId, null, null, null, null, false));
      var versions = daos.vvd.deleteWhereVidEq(vid);
      var value = daos.vld.deleteWhereIdEq(vid);
      cmd.val.vid = versions.cmd.rowCount + value.cmd.rowCount;
      return cmd;
    } catch (Exception e) {
      return cmd.withError(e);
    }
  }

}
