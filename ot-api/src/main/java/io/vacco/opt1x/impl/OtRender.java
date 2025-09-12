package io.vacco.opt1x.impl;

import io.vacco.opt1x.dto.OtVar;
import io.vacco.opt1x.schema.OtNodeType;
import io.vacco.opt1x.schema.OtValueType;
import java.util.*;

public class OtRender {

  private static Object parse(String value, OtValueType type) {
    switch (type) {
      case String: return value;
      case Boolean: return Boolean.parseBoolean(value);
      case Number:
        try {
          return Long.parseLong(value);
        } catch (NumberFormatException e) {
          return Double.parseDouble(value);
        }
      default:
        throw new IllegalStateException("Unknown value type");
    }
  }

  private static Object buildValue(OtVar current, Map<Integer, OtVar> nidMap, Map<Integer, List<Integer>> children) {
    var type = current.node.type;
    if (type == OtNodeType.Value) {
      if (current.val == null) {
        throw new IllegalStateException("Value node without val");
      }
      return parse(current.val.val, current.val.type);
    } else {
      var ch = children.getOrDefault(current.node.nid, Collections.emptyList());
      ch.sort(Comparator.comparingInt(nid -> {
        var idx = nidMap.get(nid).node.itemIdx;
        if (idx == null) {
          throw new IllegalStateException("Null itemIdx for child " + nid);
        }
        return idx;
      }));
      if (type == OtNodeType.Object) {
        var m = new LinkedHashMap<String, Object>();
        for (var cnid : ch) {
          var cv = nidMap.get(cnid);
          m.put(cv.node.label, buildValue(cv, nidMap, children));
        }
        return m;
      } else if (type == OtNodeType.Array) {
        var l = new ArrayList<>();
        for (var cNid : ch) {
          var cv = nidMap.get(cNid);
          l.add(buildValue(cv, nidMap, children));
        }
        return l;
      }
    }
    throw new IllegalStateException("Unknown type");
  }

  public static Map<String, Object> toMap(List<OtVar> tree) {
    if (tree.isEmpty()) {
      return Collections.emptyMap();
    }
    var nidMap = new HashMap<Integer, OtVar>();
    var children = new HashMap<Integer, List<Integer>>();
    var rootNid = (Integer) null;
    for (var v : tree) {
      nidMap.put(v.node.nid, v);
      if (v.node.pNid == null) {
        rootNid = v.node.nid;
      } else {
        children.computeIfAbsent(v.node.pNid, k -> new ArrayList<>()).add(v.node.nid);
      }
    }
    if (rootNid == null) {
      throw new IllegalArgumentException("No root node found");
    }
    var root = nidMap.get(rootNid);
    if (root.node.type != OtNodeType.Object) {
      throw new IllegalArgumentException("Root must be Object");
    }
    var result = new LinkedHashMap<String, Object>();
    var rootChildren = children.getOrDefault(rootNid, Collections.emptyList());
    rootChildren.sort(Comparator.comparingInt(nid -> {
      var idx = nidMap.get(nid).node.itemIdx;
      if (idx == null) {
        throw new IllegalStateException("Null itemIdx for child " + nid);
      }
      return idx;
    }));
    for (var childNid : rootChildren) {
      var child = nidMap.get(childNid);
      result.put(child.node.label, buildValue(child, nidMap, children));
    }
    return result;
  }

}
