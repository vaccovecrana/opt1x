package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtNamespace {

  @MtPk @MtDao(loadIn = true)
  public Integer nsId;

  @MtFk(OtNamespace.class)
  @MtDao(loadEq = true)
  public Integer pNsId;

  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String name;

  @MtPk(idx = 0) @MtUnique
  @MtVarchar(256) @MtNotNull
  @MtDao(loadEq = true)
  public String path;

  @MtCol @MtDao
  public long createUtcMs;

  public static OtNamespace namespace(Integer pNsId, String name) {
    var ns = new OtNamespace();
    ns.pNsId = pNsId;
    ns.name = name;
    return ns;
  }

  public static OtNamespace namespace(Integer pNsId, String name, String path, long createUtcMs) {
    var ns = namespace(pNsId, name);
    ns.path = path;
    ns.createUtcMs = createUtcMs;
    return ns;
  }

  @Override public String toString() {
    return String.format(
      "%d, %d, %s, %s",
      nsId, pNsId, name, path
    );
  }

  @Override public boolean equals(Object obj) {
    return obj instanceof OtNamespace
      && ((OtNamespace) obj).nsId.equals(this.nsId);
  }

  @Override public int hashCode() {
    return nsId;
  }

}
