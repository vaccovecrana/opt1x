package io.vacco.opt1x.spring;

import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.OtNodeType;
import java.util.*;

public class OtConfigOpMerge {

  public static void merge(OtConfigOpIdx op0, OtConfigOpIdx op1, String path) {
    var isRoot = op0.isRoot(path);
    var p0 = op0.parentOf(path);
    var v0 = op0.varOf(path);
    var v1 = op1.varOf(path);
    var match = v0 != null && v1 != null;
    var noVal = v0 != null && v0.val == null;
    var v0Arr = v0 != null && v0.node.type == OtNodeType.Array;
    var p0Arr = p0 != null && p0.node.type == OtNodeType.Array;
    if (isRoot || (match && noVal && !v0Arr) || p0Arr) {
      return;
    }
    if (match) {
      if (v0Arr) {
        op0.replace(path, v1);
        var children = op1.childrenOf(path);
        for (var e : children.entrySet()) {
          var pp = op0.parentPathOf(e.getKey());
          op0.set(e.getKey(), e.getValue(), op0.varOf(pp));
        }
      } else if (!v0.val.equals(v1.val)) {
        op0.replace(path, v1);
      }
    } else {
      op0.set(path, v1, op0.parentOf(path));
    }
  }

  public static OtConfigOpIdx merge(OtConfigOpIdx op0, OtConfigOpIdx op1) {
    for (var path : op1.cmdIdx.keySet()) {
      merge(op0, op1, path);
    }
    op0.cmd.vars = new ArrayList<>(op0.cmdIdx.values());
    return op0;
  }

  public static OtConfigOp merge(List<OtConfigOp> ops) {
    if (ops == null || ops.isEmpty()) {
      throw new IllegalArgumentException("No configs to merge");
    }
    var merged = ops.stream()
      .map(OtConfigOpIdx::new)
      .reduce(OtConfigOpMerge::merge)
      .map(op -> op.cmd);
    return merged.orElseThrow();
  }

}
