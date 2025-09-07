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
public class OtKeyGroupDao extends MtWriteDao<io.vacco.opt1x.schema.OtKeyGroup, java.lang.Integer> {

  public static final String fld_kid = "kid";
  public static final String fld_gid = "gid";
  public static final String fld_role = "role";
  public static final String fld_grantKid = "grantKid";
  public static final String fld_grantUtcMs = "grantUtcMs";

  public OtKeyGroupDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtKeyGroup.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_kid() {
    return this.dsc.getField(fld_kid);
  }

  public List<io.vacco.opt1x.schema.OtKeyGroup> loadWhereKidEq(java.lang.Integer kid) {
    return loadWhereEq(fld_kid, kid);
  }

  public MtFieldDescriptor fld_gid() {
    return this.dsc.getField(fld_gid);
  }

  public List<io.vacco.opt1x.schema.OtKeyGroup> loadWhereGidEq(java.lang.Integer gid) {
    return loadWhereEq(fld_gid, gid);
  }

  public final List<io.vacco.opt1x.schema.OtKeyGroup> listWhereGidIn(java.lang.Integer ... values) {
    return listWhereIn(fld_gid, values);
  }

  public MtResult<io.vacco.opt1x.schema.OtKeyGroup> deleteWhereGidEq(java.lang.Integer gid) {
    return deleteWhereEq(fld_gid, gid);
  }
  public MtFieldDescriptor fld_role() {
    return this.dsc.getField(fld_role);
  }

  public MtFieldDescriptor fld_grantKid() {
    return this.dsc.getField(fld_grantKid);
  }

  public MtFieldDescriptor fld_grantUtcMs() {
    return this.dsc.getField(fld_grantUtcMs);
  }

}