package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.*;
import java.util.*;

public class OtConfigOp extends OtResult {

  /** input parameter, the target configuration */
  public OtConfig cfg;
  public boolean  cfgClone;

  /** input parameter: decrypt config values when loading the configuration */
  public boolean  encrypted;

  /**
   * input/output parameter:
   *   a list of nodes to write, or a list of loaded configuration nodes
   */
  public List<OtVar> vars = new ArrayList<>();

  public transient OtApiKey key;

  public OtConfigOp withApiKey(OtApiKey key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }

  public OtConfigOp withConfig(OtConfig cfg) {
    this.cfg = Objects.requireNonNull(cfg);
    return this;
  }

  public OtConfigOp withEncrypted(boolean encrypted) {
    this.encrypted = encrypted;
    return this;
  }

  public OtConfigOp withVars(OtVar ... vars) {
    this.vars = Arrays.asList(Objects.requireNonNull(vars));
    return this;
  }

  public static OtConfigOp configOp() {
    return new OtConfigOp();
  }

}
