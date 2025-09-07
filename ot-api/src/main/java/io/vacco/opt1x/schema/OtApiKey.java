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

  @MtCol
  public boolean leaf; // whether this key can create sub keys in its path

  @MtCol @MtDao
  public long createUtcMs;

  // either last ODIC login time, or raw API key access time
  @MtCol @MtDao
  public long accessUtcMs; // TODO log access time when an API key authenticates.

  // Optional attributes
  @MtVarchar(2048) public String metadata0;
  @MtVarchar(2048) public String metadata1;
  @MtVarchar(2048) public String metadata2;
  @MtVarchar(2048) public String metadata3;

  @MtVarchar(512) @MtIndex public String oidcSub;
  @MtVarchar(512) @MtIndex public String oidcEmail;

  public static OtApiKey rawKey(Integer pKid, String name, boolean leaf) {
    var k = new OtApiKey();
    k.pKid = pKid;
    k.name = name;
    k.leaf = leaf;
    return k;
  }

  // TODO add oidcKey() method

  @Override public String toString() {
    return String.format("%d, %d, %s", kid, pKid, path);
  }

}
