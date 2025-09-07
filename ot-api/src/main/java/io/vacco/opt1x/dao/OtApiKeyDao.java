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
public class OtApiKeyDao extends MtWriteDao<io.vacco.opt1x.schema.OtApiKey, java.lang.Integer> {

  public static final String fld_kid = "kid";
  public static final String fld_name = "name";
  public static final String fld_path = "path";
  public static final String fld_hash = "hash";
  public static final String fld_createUtcMs = "createUtcMs";
  public static final String fld_accessUtcMs = "accessUtcMs";

  public OtApiKeyDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtApiKey.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_kid() {
    return this.dsc.getField(fld_kid);
  }

  public List<io.vacco.opt1x.schema.OtApiKey> loadWhereKidEq(java.lang.Integer kid) {
    return loadWhereEq(fld_kid, kid);
  }

  public final Map<java.lang.Integer, List<io.vacco.opt1x.schema.OtApiKey>> loadWhereKidIn(java.lang.Integer ... values) {
    return loadWhereIn(fld_kid, values);
  }

  public MtFieldDescriptor fld_name() {
    return this.dsc.getField(fld_name);
  }

  public List<io.vacco.opt1x.schema.OtApiKey> loadWhereNameEq(java.lang.String name) {
    return loadWhereEq(fld_name, name);
  }

  public MtFieldDescriptor fld_path() {
    return this.dsc.getField(fld_path);
  }

  public MtFieldDescriptor fld_hash() {
    return this.dsc.getField(fld_hash);
  }

  public List<io.vacco.opt1x.schema.OtApiKey> loadWhereHashEq(java.lang.String hash) {
    return loadWhereEq(fld_hash, hash);
  }

  public MtFieldDescriptor fld_createUtcMs() {
    return this.dsc.getField(fld_createUtcMs);
  }

  public MtFieldDescriptor fld_accessUtcMs() {
    return this.dsc.getField(fld_accessUtcMs);
  }

}