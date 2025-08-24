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
  public long createdAtUtcMs;

}
