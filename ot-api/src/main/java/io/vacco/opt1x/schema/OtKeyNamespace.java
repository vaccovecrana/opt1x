package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtKeyNamespace {

  @MtPk public int id;

  @MtFk(OtApiKey.class)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao
  public Integer kid;

  @MtFk(OtNamespace.class)
  @MtPk(idx = 1) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao(loadEq = true)
  public Integer nsId;

  @MtFk(OtApiKey.class)
  @MtNotNull
  @MtDao
  public Integer grantKid;

  @MtCol
  public boolean writeAccess;

  @MtCol @MtDao
  public long grantUtcMs;

  public static OtKeyNamespace keyNamespace(Integer kid, Integer nsId,
                                            Integer grantKid, boolean writeAccess) {
    var kn = new OtKeyNamespace();
    kn.kid = kid;
    kn.nsId = nsId;
    kn.grantKid = grantKid;
    kn.writeAccess = writeAccess;
    return kn;
  }

  @Override public String toString() {
    return String.format(
      "[%d, kid: %d, nsId: %d, gKid: %d, %b]",
      id, kid, nsId, grantKid, writeAccess
    );
  }

}
