package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtApiKey {

  @MtPk @MtDao(loadEq = true, loadIn = true)
  public Integer kid;

  @MtFk(OtApiKey.class)
  public Integer pKid;

  @MtVarchar(64) @MtNotNull
  @MtPk(idx = 0) @MtUnique
  @MtDao(loadEq = true)
  public String name;

  @MtVarchar(2048) @MtNotNull @MtUnique @MtDao
  public String path;

  @MtVarchar(64) @MtNotNull @MtDao(loadEq = true)
  public String hash; // SHA256 hash of the key

  @MtCol @MtNotNull @MtDao
  public OtRole role;

  @MtVarchar(2048) public String metadata0;
  @MtVarchar(2048) public String metadata1;
  @MtVarchar(2048) public String metadata2;
  @MtVarchar(2048) public String metadata3;

  @MtCol @MtDao
  public long createdAtUtcMs;

  @MtCol
  public long deletedAtUtcMs;

  public static OtApiKey of(Integer pKid, String name, String hash,
                            OtRole role, long createdAtUtcMs) {
    var k = new OtApiKey();
    k.pKid = pKid;
    k.name = name;
    k.path = String.format("/%s", name);
    k.hash = hash;
    k.role = role;
    k.createdAtUtcMs = createdAtUtcMs;
    return k;
  }

  @Override public String toString() {
    return String.format(
      "%d, %d, %s, %s",
      kid, pKid, path, role
    );
  }

}
