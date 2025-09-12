package io.vacco.opt1x.spring;

import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.OtNodeType;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

public class OtConfigOpIdx {

  public static final String fs = "/";

  public final OtConfigOp           cmd;
  public final Map<String, OtVar>   cmdIdx = new TreeMap<>();
  public final Map<Integer, OtVar>  idIdx = new HashMap<>();
  public final Map<Integer, String> idpIdx = new HashMap<>();

  public OtConfigOpIdx(OtConfigOp cmd) {
    this.cmd = Objects.requireNonNull(cmd);
    for (var v : cmd.vars) {
      idIdx.put(v.node.nid, v);
    }
    for (var v : cmd.vars) {
      var p = new ArrayList<String>();
      var v0 = v;
      while (v0 != null) {
        p.add(v0.node.label);
        v0 = idIdx.get(v0.node.pNid);
      }
      Collections.reverse(p);
      var path = String.join(fs, p);
      cmdIdx.put(path, v);
      if (v != null) {
        idpIdx.put(v.node.nid, path);
      }
    }
  }

  public OtVar varOf(String path) {
    if (path != null) {
      return cmdIdx.get(path);
    }
    return null;
  }

  public boolean isRoot(String path) {
    var v = cmdIdx.get(path);
    return v != null && v.node.pNid == null;
  }

  public boolean isArray(String path) {
    return varOf(path) != null && varOf(path).node.type == OtNodeType.Array;
  }

  public String parentPathOf(String path) {
    if (!path.contains(fs)) {
      return null;
    }
    var pm = new TreeMap<Integer, String>();
    for (var p : this.cmdIdx.keySet()) {
      if (path.contains(p)) {
        pm.put(p.split(fs).length, p);
      }
    }
    var segs = path.split(fs);
    for (var e : pm.entrySet()) {
      if (e.getKey() == segs.length - 1) {
        return e.getValue();
      }
    }
    return null;
  }

  public OtVar parentOf(String path) {
    var pp = parentPathOf(path);
    return varOf(pp);
  }

  public Map<String, OtVar> childrenOf(String path) {
    return new TreeMap<>(
      cmdIdx.keySet().stream()
        .filter(p0 -> p0.startsWith(path))
        .filter(p0 -> !p0.equals(path))
        .collect(Collectors.toMap(Function.identity(), cmdIdx::get))
    );
  }

  public void remove(String path) {
    var v0 = cmdIdx.remove(path);
    idIdx.remove(v0.node.nid);
    idpIdx.remove(v0.node.nid);
  }

  public void set(String path, OtVar v, OtVar p) {
    cmdIdx.put(path, v);
    idIdx.put(v.node.nid, v);
    idpIdx.put(v.node.nid, path);
    v.node.pNid = p.node.nid;
  }

  public void replace(String path, OtVar v) {
    var p = parentOf(path);
    if (isArray(path)) {
      var paths = cmdIdx.keySet().stream()
        .filter(p0 -> p0.startsWith(path))
        .collect(Collectors.toList());
      for (var p0 : paths) {
        remove(p0);
      }
    } else {
      remove(path);
    }
    set(path, v, p);
  }

}
