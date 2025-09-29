package io.vacco.opt1x.dto;

import io.vacco.metolithe.util.MtPage1;
import io.vacco.opt1x.schema.*;
import java.util.*;

public class OtValueOp extends OtResult {

  public transient OtApiKey key;

  /** input/output parameter */
  public OtValue val;
  public boolean updated;

  /** output parameter, list of value versions */
  public List<OtValueVer> valVersions = new ArrayList<>();

  /** Output parameters, used during raw value editing. */
  public MtPage1<OtValue, String> valPage;
  public OtNamespace namespace;

  /**
   * Output parameters, used during config editing.
   * Namespaces/values accessible to the active user
   */
  public List<OtValue>     values;
  public List<OtNamespace> namespaces;

  public OtValueOp withKey(OtApiKey key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }

  public OtValueOp withVal(OtValue val) {
    this.val = Objects.requireNonNull(val);
    return this;
  }

  public static OtValueOp valueOp() {
    return new OtValueOp();
  }

}