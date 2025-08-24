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
public class OtNodeDao extends MtWriteDao<io.vacco.opt1x.schema.OtNode, java.lang.Integer> {

  public static final String fld_cid = "cid";
  public static final String fld_label = "label";
  public static final String fld_type = "type";
  public static final String fld_vid = "vid";

  public OtNodeDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtNode.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_cid() {
    return this.dsc.getField(fld_cid);
  }

  public List<io.vacco.opt1x.schema.OtNode> loadWhereCidEq(java.lang.Integer cid) {
    return loadWhereEq(fld_cid, cid);
  }

  public MtResult<io.vacco.opt1x.schema.OtNode> deleteWhereCidEq(java.lang.Integer cid) {
    return deleteWhereEq(fld_cid, cid);
  }
  public MtFieldDescriptor fld_label() {
    return this.dsc.getField(fld_label);
  }

  public MtFieldDescriptor fld_type() {
    return this.dsc.getField(fld_type);
  }

  public MtFieldDescriptor fld_vid() {
    return this.dsc.getField(fld_vid);
  }

}