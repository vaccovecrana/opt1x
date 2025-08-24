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
public class OtConfigDao extends MtWriteDao<io.vacco.opt1x.schema.OtConfig, java.lang.Integer> {

  public static final String fld_cid = "cid";
  public static final String fld_nsId = "nsId";
  public static final String fld_name = "name";

  public OtConfigDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtConfig.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_cid() {
    return this.dsc.getField(fld_cid);
  }

  public MtFieldDescriptor fld_nsId() {
    return this.dsc.getField(fld_nsId);
  }

  public List<io.vacco.opt1x.schema.OtConfig> loadWhereNsIdEq(java.lang.Integer nsId) {
    return loadWhereEq(fld_nsId, nsId);
  }

  public MtFieldDescriptor fld_name() {
    return this.dsc.getField(fld_name);
  }

  public List<io.vacco.opt1x.schema.OtConfig> loadWhereNameEq(java.lang.String name) {
    return loadWhereEq(fld_name, name);
  }

}