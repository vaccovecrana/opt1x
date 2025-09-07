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
public class OtGroupNsDao extends MtWriteDao<io.vacco.opt1x.schema.OtGroupNs, java.lang.Integer> {

  public static final String fld_gid = "gid";
  public static final String fld_nsId = "nsId";
  public static final String fld_grantKid = "grantKid";
  public static final String fld_grantUtcMs = "grantUtcMs";
  public static final String fld_read = "read";
  public static final String fld_write = "write";
  public static final String fld_manage = "manage";

  public OtGroupNsDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtGroupNs.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_gid() {
    return this.dsc.getField(fld_gid);
  }

  public List<io.vacco.opt1x.schema.OtGroupNs> loadWhereGidEq(java.lang.Integer gid) {
    return loadWhereEq(fld_gid, gid);
  }

  public final Map<java.lang.Integer, List<io.vacco.opt1x.schema.OtGroupNs>> loadWhereGidIn(java.lang.Integer ... values) {
    return loadWhereIn(fld_gid, values);
  }

  public MtResult<io.vacco.opt1x.schema.OtGroupNs> deleteWhereGidEq(java.lang.Integer gid) {
    return deleteWhereEq(fld_gid, gid);
  }
  public MtFieldDescriptor fld_nsId() {
    return this.dsc.getField(fld_nsId);
  }

  public List<io.vacco.opt1x.schema.OtGroupNs> loadWhereNsIdEq(java.lang.Integer nsId) {
    return loadWhereEq(fld_nsId, nsId);
  }

  public final List<io.vacco.opt1x.schema.OtGroupNs> listWhereNsIdIn(java.lang.Integer ... values) {
    return listWhereIn(fld_nsId, values);
  }

  public MtFieldDescriptor fld_grantKid() {
    return this.dsc.getField(fld_grantKid);
  }

  public MtFieldDescriptor fld_grantUtcMs() {
    return this.dsc.getField(fld_grantUtcMs);
  }

  public MtFieldDescriptor fld_read() {
    return this.dsc.getField(fld_read);
  }

  public MtFieldDescriptor fld_write() {
    return this.dsc.getField(fld_write);
  }

  public MtFieldDescriptor fld_manage() {
    return this.dsc.getField(fld_manage);
  }

}