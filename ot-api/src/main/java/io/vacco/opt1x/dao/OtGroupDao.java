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
public class OtGroupDao extends MtWriteDao<io.vacco.opt1x.schema.OtGroup, java.lang.Integer> {

  public static final String fld_gid = "gid";
  public static final String fld_pGid = "pGid";
  public static final String fld_name = "name";
  public static final String fld_path = "path";
  public static final String fld_createUtcMs = "createUtcMs";

  public OtGroupDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtGroup.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_gid() {
    return this.dsc.getField(fld_gid);
  }

  public final List<io.vacco.opt1x.schema.OtGroup> listWhereGidIn(java.lang.Integer ... values) {
    return listWhereIn(fld_gid, values);
  }

  public MtResult<io.vacco.opt1x.schema.OtGroup> deleteWhereGidEq(java.lang.Integer gid) {
    return deleteWhereEq(fld_gid, gid);
  }
  public MtFieldDescriptor fld_pGid() {
    return this.dsc.getField(fld_pGid);
  }

  public List<io.vacco.opt1x.schema.OtGroup> loadWherePGidEq(java.lang.Integer pGid) {
    return loadWhereEq(fld_pGid, pGid);
  }

  public MtFieldDescriptor fld_name() {
    return this.dsc.getField(fld_name);
  }

  public List<io.vacco.opt1x.schema.OtGroup> loadWhereNameEq(java.lang.String name) {
    return loadWhereEq(fld_name, name);
  }

  public MtFieldDescriptor fld_path() {
    return this.dsc.getField(fld_path);
  }

  public List<io.vacco.opt1x.schema.OtGroup> loadWherePathEq(java.lang.String path) {
    return loadWhereEq(fld_path, path);
  }

  public MtFieldDescriptor fld_createUtcMs() {
    return this.dsc.getField(fld_createUtcMs);
  }

}