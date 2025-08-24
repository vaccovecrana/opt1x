package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtNode;
import io.vacco.opt1x.schema.OtValue;

import java.util.Objects;

public class OtVar {

  public OtNode  node;
  public OtValue val;

  public static OtVar of(OtNode node, OtValue val) {
    var v = new OtVar();
    v.node = Objects.requireNonNull(node);
    v.val = val;
    return v;
  }

  @Override public String toString() {
    return String.format("%s / %s", node, val);
  }

}
