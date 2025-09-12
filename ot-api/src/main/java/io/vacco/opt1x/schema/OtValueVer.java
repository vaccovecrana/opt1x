package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtValueVer {

  @MtPk @MtDao
  public Integer vvId;

  @MtFk(OtValue.class) @MtNotNull
  @MtDao(loadEq = true, deleteEq = true)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  public Integer vid;

  @MtVarchar(4096) @MtNotNull
  @MtPk(idx = 1) @MtUnique(idx = 0)
  public String val;

  @MtCol @MtNotNull
  @MtPk(idx = 2) @MtUnique(idx = 0)
  public OtValueType type;

  @MtVarchar(4096)
  public String notes;

  @MtCol
  public long changeUtcMs; // When the change occurred

  public static OtValueVer version(OtValue v, long changeUtcMs) {
    var vv = new OtValueVer();
    vv.vid = v.vid;
    vv.val = v.val;
    vv.type = v.type;
    vv.notes = v.notes;
    vv.changeUtcMs = changeUtcMs;
    return vv;
  }

}