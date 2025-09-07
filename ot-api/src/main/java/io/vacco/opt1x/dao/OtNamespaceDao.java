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
public class OtNamespaceDao extends MtWriteDao<io.vacco.opt1x.schema.OtNamespace, java.lang.Integer> {

  public static final String fld_nsId = "nsId";
  public static final String fld_pNsId = "pNsId";
  public static final String fld_name = "name";
  public static final String fld_path = "path";
  public static final String fld_createUtcMs = "createUtcMs";

  public OtNamespaceDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtNamespace.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_nsId() {
    return this.dsc.getField(fld_nsId);
  }

  public final Map<java.lang.Integer, List<io.vacco.opt1x.schema.OtNamespace>> loadWhereNsIdIn(java.lang.Integer ... values) {
    return loadWhereIn(fld_nsId, values);
  }

  public MtFieldDescriptor fld_pNsId() {
    return this.dsc.getField(fld_pNsId);
  }

  public List<io.vacco.opt1x.schema.OtNamespace> loadWherePNsIdEq(java.lang.Integer pNsId) {
    return loadWhereEq(fld_pNsId, pNsId);
  }

  public MtFieldDescriptor fld_name() {
    return this.dsc.getField(fld_name);
  }

  public List<io.vacco.opt1x.schema.OtNamespace> loadWhereNameEq(java.lang.String name) {
    return loadWhereEq(fld_name, name);
  }

  public MtFieldDescriptor fld_path() {
    return this.dsc.getField(fld_path);
  }

  public List<io.vacco.opt1x.schema.OtNamespace> loadWherePathEq(java.lang.String path) {
    return loadWhereEq(fld_path, path);
  }

  public MtFieldDescriptor fld_createUtcMs() {
    return this.dsc.getField(fld_createUtcMs);
  }

}