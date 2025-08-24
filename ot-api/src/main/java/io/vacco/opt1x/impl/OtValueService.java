package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.ArrayList;
import java.util.Objects;

import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.lang.String.format;

public class OtValueService {

  public  final OtDaos daos;
  private final OtNamespaceService nsService;
  private final OtSealService sealService;

  public OtValueService(OtDaos daos, OtNamespaceService nsService, OtSealService sealService) {
    this.daos = Objects.requireNonNull(daos);
    this.nsService = Objects.requireNonNull(nsService);
    this.sealService = Objects.requireNonNull(sealService);
  }

  public OtList<OtValue, String> valuesOf(Integer kid, Integer nsId, int pageSize, String next) {
    var out = new OtList<OtValue, String>();
    try {
      out = nsService.nsAccess(out, kid, nsId, false);
      if (!out.ok()) {
        return out;
      }
      var nsIdFld = daos.vld.fld_nsId();
      out.page = daos.vld.loadPage1(
        pageSize, false,
        daos.vld.query().eq(nsIdFld, nsId),
        OtValueDao.fld_name, next
      );
      return out;
    } catch (Exception e) {
      onError("Value list error", e);
      return out.withError(e);
    }
  }

  public OtValueOp duplicate(OtValueOp cmd) {
    var values = daos.vld.loadWhereNsIdEq(cmd.val.nsId);
    for (var val : values) {
      if (val.name.equals(cmd.val.name)) {
        cmd = cmd
          .withVal(val)
          .withError(format("Value [%s] already exists in namespace [%s]", cmd.val.name, cmd.val.nsId));
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
      cmd = OtValid
        .validate(Objects.requireNonNull(cmd).val, cmd)
        .validate(this::duplicate)
        .validate(this::valueType)
        .validate(cmd0 -> nsService.nsAccess(cmd0, cmd0.key.kid, cmd0.val.nsId, true));
      if (cmd.ok()) {
        cmd.val.createdAtUtcMs = System.currentTimeMillis();
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
      cmd.namespaces = new ArrayList<>();
      cmd.values = new ArrayList<>();
      var nsRes = nsService.loadNamespacesOf(key.kid, 1000, null); // TODO do these numbers need to be tweakable?
      if (!nsRes.ok()) {
        return cmd.withError(nsRes.error);
      }
      cmd.namespaces.addAll(nsRes.page.items);
      for (var ns : nsRes.page.items) {
        var valRes = valuesOf(key.kid, ns.nsId, 1000, null);
        if (!valRes.ok()) {
          return cmd.withError(valRes.error);
        }
        cmd.values.addAll(valRes.page.items);
      }
      return cmd;
    } catch (Exception e) {
      onError("Value user access error", e);
      return cmd.withError(e);
    }
  }

}
