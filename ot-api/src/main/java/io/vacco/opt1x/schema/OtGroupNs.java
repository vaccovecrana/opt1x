package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtGroupNs {

  @MtPk public int id;

  @MtFk(OtGroup.class)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao(loadEq = true, loadIn = true, deleteEq = true)
  public Integer gid; // target group assigned

  @MtFk(OtNamespace.class)
  @MtPk(idx = 1) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao(loadEq = true, listIn = true)
  public Integer nsId; // target namespace accessed

  @MtFk(OtApiKey.class)
  @MtNotNull
  @MtDao
  public Integer grantKid; // who granted this access

  @MtCol @MtDao
  public long grantUtcMs; // grant time

  @MtCol @MtDao public boolean read;
  @MtCol @MtDao public boolean write;
  @MtCol @MtDao public boolean manage;

  public static OtGroupNs groupNs(Integer gid, Integer nsId, Integer grantKid,
                                  boolean read, boolean write, boolean manage) {
    var gn = new OtGroupNs();
    gn.gid = gid;
    gn.nsId = nsId;
    gn.grantKid = grantKid;
    gn.read = read;
    gn.write = write;
    gn.manage = manage;
    return gn;
  }

  public static String flagsOf(boolean r, boolean w, boolean m) {
    return String.format(
      "%s%s%s",
      r ? "r" : "-",
      w ? "w" : "-",
      m ? "m" : "-"
    );
  }

  @Override public String toString() {
    return String.format(
      "[%d, gid: %d, ns: %d, gKid: %d, %s]",
      id, gid, nsId, grantKid,
      flagsOf(read, write, manage)
    );
  }

  @Override public boolean equals(Object obj) {
    return obj instanceof OtGroupNs
      && ((OtGroupNs) obj).id == this.id;
  }

  @Override public int hashCode() {
    return this.id;
  }

}