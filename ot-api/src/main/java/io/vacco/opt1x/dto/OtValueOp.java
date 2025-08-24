package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtApiKey;
import io.vacco.opt1x.schema.OtNamespace;
import io.vacco.opt1x.schema.OtValue;
import java.util.List;
import java.util.Objects;

public class OtValueOp extends OtResult {

  public transient OtApiKey key;

  /** input/output parameter */
  public OtValue val;

  /**
   * Output parameters, used during config editing.
   * Namespaces/values accessible to the active user
   */
  public List<OtValue> values;
  public List<OtNamespace> namespaces;

  public OtValueOp withApiKey(OtApiKey key) {
    this.key = Objects.requireNonNull(key);
    return this;
  }

  public OtValueOp withVal(OtValue val) {
    this.val = Objects.requireNonNull(val);
    return this;
  }

  public static OtValueOp valCmd(OtApiKey key, OtValue val) {
    var op = new OtValueOp();
    op.val = Objects.requireNonNull(val);
    op.key = Objects.requireNonNull(key);
    return op;
  }

}