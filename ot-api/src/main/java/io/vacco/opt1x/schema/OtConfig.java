package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtConfig {

  @MtPk @MtDao public Integer cid;

  @MtFk(OtNamespace.class) @MtNotNull
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtDao(loadEq = true)
  public Integer nsId;

  @MtVarchar(256) @MtNotNull
  @MtPk(idx = 1) @MtUnique(idx = 0)
  @MtDao(loadEq = true)
  public String name;

  @MtCol
  public long createUtcMs;

  public static OtConfig config(Integer cid, Integer nsId, String name) {
    var c = new OtConfig();
    c.cid = cid;
    c.nsId = nsId;
    c.name = name;
    return c;
  }

  @Override public String toString() {
    return String.format("%d %s", cid, name);
  }

}
