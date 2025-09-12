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
public class OtValueDao extends MtWriteDao<io.vacco.opt1x.schema.OtValue, java.lang.Integer> {

  public static final String fld_vid = "vid";
  public static final String fld_nsId = "nsId";
  public static final String fld_name = "name";
  public static final String fld_val = "val";
  public static final String fld_type = "type";
  public static final String fld_encrypted = "encrypted";
  public static final String fld_createUtcMs = "createUtcMs";

  public OtValueDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtValue.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_vid() {
    return this.dsc.getField(fld_vid);
  }

  public final Map<java.lang.Integer, List<io.vacco.opt1x.schema.OtValue>> loadWhereVidIn(java.lang.Integer ... values) {
    return loadWhereIn(fld_vid, values);
  }

  public MtFieldDescriptor fld_nsId() {
    return this.dsc.getField(fld_nsId);
  }

  public List<io.vacco.opt1x.schema.OtValue> loadWhereNsIdEq(java.lang.Integer nsId) {
    return loadWhereEq(fld_nsId, nsId);
  }

  public final List<io.vacco.opt1x.schema.OtValue> listWhereNsIdIn(java.lang.Integer ... values) {
    return listWhereIn(fld_nsId, values);
  }

  public MtFieldDescriptor fld_name() {
    return this.dsc.getField(fld_name);
  }

  public List<io.vacco.opt1x.schema.OtValue> loadWhereNameEq(java.lang.String name) {
    return loadWhereEq(fld_name, name);
  }

  public MtFieldDescriptor fld_val() {
    return this.dsc.getField(fld_val);
  }

  public MtFieldDescriptor fld_type() {
    return this.dsc.getField(fld_type);
  }

  public MtFieldDescriptor fld_encrypted() {
    return this.dsc.getField(fld_encrypted);
  }

  public MtFieldDescriptor fld_createUtcMs() {
    return this.dsc.getField(fld_createUtcMs);
  }

}