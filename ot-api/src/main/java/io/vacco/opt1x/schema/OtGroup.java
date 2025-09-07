package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtGroup {

  @MtPk @MtDao(listIn = true, deleteEq = true)
  public Integer gid;

  @MtFk(OtGroup.class)
  @MtDao(loadEq = true)
  public Integer pGid;

  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String name;

  @MtPk(idx = 0) @MtUnique
  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String path;

  @MtCol @MtDao
  public long createUtcMs;

  public static OtGroup group(Integer pGid, String name, String path, long createUtcMs) {
    var g = new OtGroup();
    g.pGid = pGid;
    g.name = name;
    g.path = path;
    g.createUtcMs = createUtcMs;
    return g;
  }

  public static OtGroup group(Integer pGid, String name) {
    return group(pGid, name, null, 0);
  }

  @Override public String toString() {
    return String.format("%d %s", gid, path);
  }

  @Override public boolean equals(Object obj) {
    return obj instanceof OtGroup
      && ((OtGroup) obj).path.equals(this.path);
  }

  @Override public int hashCode() {
    return gid;
  }

}
