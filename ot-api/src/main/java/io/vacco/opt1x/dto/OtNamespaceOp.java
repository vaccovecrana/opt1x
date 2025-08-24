package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.OtKeyNamespace;
import io.vacco.opt1x.schema.OtNamespace;
import java.util.Objects;

public class OtNamespaceOp extends OtResult {

  // input/output parameters
  public OtNamespace namespace;
  public OtKeyNamespace keyNamespace;

  public OtNamespaceOp withKeyNamespace(OtKeyNamespace keyNamespace) {
    this.keyNamespace = Objects.requireNonNull(keyNamespace);
    return this;
  }

  public static OtNamespaceOp nsCreate(Integer ownerKid, String name, Integer parentNsId, boolean writeAccess) {
    var cmd = new OtNamespaceOp();
    cmd.namespace = OtNamespace.namespace(parentNsId, name, null);
    cmd.keyNamespace = OtKeyNamespace.keyNamespace(ownerKid, null, ownerKid, writeAccess);
    return cmd;
  }

  public static OtNamespaceOp nsAssign(Integer forKid, Integer grantKid, Integer nsId, boolean writeAccess) {
    var cmd = new OtNamespaceOp();
    cmd.namespace = new OtNamespace();
    cmd.namespace.nsId = nsId;
    cmd.keyNamespace = OtKeyNamespace.keyNamespace(forKid, nsId, grantKid, writeAccess);
    return cmd;
  }

}
