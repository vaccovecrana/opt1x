package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtApiKey;
import java.util.Objects;

public class OtApiKeyOp extends OtResult {

  // input/output parameter
  public OtApiKey key; // new key to add

  // output parameter, temporary field for client response only
  public String   raw;

  public OtApiKeyOp withKey(OtApiKey key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }

  public OtApiKeyOp withRaw(String raw) {
    this.raw = Objects.requireNonNull(raw);
    return this;
  }

  public static OtApiKeyOp keyOp() {
    return new OtApiKeyOp();
  }

}
