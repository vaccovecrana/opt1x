package io.vacco.opt1x.dao;

import io.vacco.metolithe.core.MtCaseFormat;
import io.vacco.metolithe.core.MtDescriptor;
import io.vacco.metolithe.core.MtFieldDescriptor;
import io.vacco.metolithe.id.MtIdFn;
import io.vacco.metolithe.dao.MtWriteDao;
import io.vacco.metolithe.query.MtJdbc;
import io.vacco.metolithe.query.MtResult;

import java.util.List;
import java.util.Map;

/**************************************************
 * Generated source file. Do not modify directly. *
 **************************************************/
public class OtKeyNamespaceDao extends MtWriteDao<io.vacco.opt1x.schema.OtKeyNamespace, java.lang.Integer> {

  public static final String fld_kid = "kid";
  public static final String fld_nsId = "nsId";
  public static final String fld_grantKid = "grantKid";
  public static final String fld_grantUtcMs = "grantUtcMs";

  public OtKeyNamespaceDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtKeyNamespace.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_kid() {
    return this.dsc.getField(fld_kid);
  }

  public MtFieldDescriptor fld_nsId() {
    return this.dsc.getField(fld_nsId);
  }

  public List<io.vacco.opt1x.schema.OtKeyNamespace> loadWhereNsIdEq(java.lang.Integer nsId) {
    return loadWhereEq(fld_nsId, nsId);
  }

  public MtFieldDescriptor fld_grantKid() {
    return this.dsc.getField(fld_grantKid);
  }

  public MtFieldDescriptor fld_grantUtcMs() {
    return this.dsc.getField(fld_grantUtcMs);
  }

}