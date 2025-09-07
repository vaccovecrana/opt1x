package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtKeyGroup {

  @MtPk public int id;

  @MtFk(OtApiKey.class)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao(loadEq = true)
  public Integer kid;       // The API key being assigned to the group

  @MtFk(OtGroup.class)
  @MtPk(idx = 1) @MtUnique(idx = 0)
  @MtNotNull
  @MtDao(loadEq = true, listIn = true, deleteEq = true)
  public Integer gid;       // The group being assigned

  @MtCol @MtDao
  public OtGroupRole role;  // The role assigned within the group

  @MtFk(OtApiKey.class)
  @MtNotNull
  @MtDao
  public Integer grantKid;  // Who granted this assignment

  @MtCol @MtDao
  public long grantUtcMs;   // When granted

  public static OtKeyGroup keyGroup(Integer kid, Integer gid, OtGroupRole role, Integer grantKid) {
    var kg = new OtKeyGroup();
    kg.kid = kid;
    kg.gid = gid;
    kg.role = role;
    kg.grantKid = grantKid;
    return kg;
  }

  @Override public String toString() {
    return String.format(
      "[%d, kid: %d, gid: %d, %s, gKid: %d]",
      id, kid, gid, role, grantKid
    );
  }

}