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
public class OtValueVerDao extends MtWriteDao<io.vacco.opt1x.schema.OtValueVer, java.lang.Integer> {

  public static final String fld_vvId = "vvId";
  public static final String fld_vid = "vid";

  public OtValueVerDao(String schema, MtCaseFormat fmt, MtJdbc jdbc, MtIdFn<java.lang.Integer> idFn) {
    super(schema, jdbc, new MtDescriptor<>(io.vacco.opt1x.schema.OtValueVer.class, fmt), idFn);
  }

  public MtFieldDescriptor fld_vvId() {
    return this.dsc.getField(fld_vvId);
  }

  public MtFieldDescriptor fld_vid() {
    return this.dsc.getField(fld_vid);
  }

  public List<io.vacco.opt1x.schema.OtValueVer> loadWhereVidEq(java.lang.Integer vid) {
    return loadWhereEq(fld_vid, vid);
  }

  public MtResult<io.vacco.opt1x.schema.OtValueVer> deleteWhereVidEq(java.lang.Integer vid) {
    return deleteWhereEq(fld_vid, vid);
  }

}