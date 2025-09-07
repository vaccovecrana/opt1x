package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;

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

  public OtValueOp duplicate(OtValueOp cmd) {
    var values = daos.vld.loadWhereNsIdEq(cmd.val.nsId);
    for (var val : values) {
      if (val.name.equals(cmd.val.name)) {
        var err = format("Value [%s] already exists in namespace [%s]", cmd.val.name, cmd.val.nsId);
        cmd = cmd.withVal(val).withError(err);
        break;
      }
    }
    return cmd;
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
    if (cmd.val.type == OtValueType.Number && !isNumber(cmd.val.value)) {
      return cmd.withError(format("Value [%s] is not a number", cmd.val.value));
    }
    if (cmd.val.type == OtValueType.Boolean && !isBoolean(cmd.val.value)) {
      return cmd.withError(format("Value [%s] is not a boolean", cmd.val.value));
    }
    return cmd;
  }

  public OtValueOp createValue(OtValueOp cmd) {
    try {
      var kg = admService.canAccessNs(Objects.requireNonNull(cmd).key.kid, cmd.val.nsId, false, true, false);
      if (kg.isEmpty()) {
        return admService.noNsAccess(cmd, cmd.key.kid, cmd.val.nsId, write);
      }
      cmd.val.createUtcMs = System.currentTimeMillis();
      cmd = OtValid
        .validate(Objects.requireNonNull(cmd).val, cmd)
        .validate(this::duplicate)
        .validate(this::valueType);
      if (cmd.ok()) {
        if (cmd.val.encrypted) {
          cmd.val = sealService.encrypt(cmd.val);
        }
        daos.vld.save(cmd.val);
      }
      return cmd;
    } catch (Exception e) {
      onError("Value create error", e);
      return cmd.withError(e);
    }
  }

  public OtValueOp accessibleValuesFor(OtApiKey key) {
    var cmd = new OtValueOp();
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

}
