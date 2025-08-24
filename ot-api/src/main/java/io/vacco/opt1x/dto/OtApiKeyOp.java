package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtApiKey;
import io.vacco.opt1x.schema.OtRole;
import java.util.Objects;

public class OtApiKeyOp extends OtResult {

  // input parameters
  public Integer parentKid;
  public String name;
  public OtRole role;

  // output parameters
  public OtApiKey key;
  public String raw; // Temporary field for client response only

  public OtApiKeyOp withKey(OtApiKey key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }

  public OtApiKeyOp withRaw(String raw) {
    this.raw = Objects.requireNonNull(raw);
    return this;
  }

  public static OtApiKeyOp keyCmd(Integer parentKid, String name, OtRole role) {
    var c = new OtApiKeyOp();
    c.parentKid = parentKid;
    c.name = name;
    c.role = role;
    return c;
  }

  public static OtApiKeyOp updateCmd(OtApiKey key) {
    var c = new OtApiKeyOp();
    c.key = Objects.requireNonNull(key);
    return c;
  }

}
