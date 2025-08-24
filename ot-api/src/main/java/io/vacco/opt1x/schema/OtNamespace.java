package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtNamespace {

  @MtPk @MtDao public Integer nsId;

  @MtFk(OtNamespace.class)
  public Integer pNsId;

  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String name;

  @MtPk(idx = 0) @MtUnique
  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String path;

  @MtCol @MtDao
  public long createdAtUtcMs;

  public OtNamespace createdAt(long utcMs) {
    this.createdAtUtcMs = utcMs;
    return this;
  }

  public static OtNamespace namespace(Integer pNsId, String name, String path) {
    var ns = new OtNamespace();
    ns.pNsId = pNsId;
    ns.name = name;
    ns.path = path;
    return ns;
  }

  @Override public String toString() {
    return String.format(
      "%d, %d, %s, %s",
      nsId, pNsId, name, path
    );
  }

}
