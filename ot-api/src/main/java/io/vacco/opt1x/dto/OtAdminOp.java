package io.vacco.opt1x.dto;

import io.vacco.opt1x.schema.*;

public class OtAdminOp extends OtResult {

  public OtApiKey       key;          // admin api key
  public OtKeyGroup     keyGroup;     // the admin key's key/group binding for management ops
  public OtKeyGroup     keyGroupBind; // a new key's group binding result

  public OtNamespace    ns;           // a target binding namespace
  public OtGroup        group;        // a target binding group
  public OtGroupNs      groupNs;      // a group/namespace binding result

  public OtAdminOp withKey(OtApiKey key) {
    this.key = key;
    return this;
  }

  public OtAdminOp withKeyGroup(OtKeyGroup kg) {
    this.keyGroup = kg;
    return this;
  }

  public OtAdminOp withKeyGroupBind(OtKeyGroup kgb) {
    this.keyGroupBind = kgb;
    return this;
  }

  public OtAdminOp withNs(OtNamespace ns) {
    this.ns = ns;
    return this;
  }

  public OtAdminOp withGroup(OtGroup group) {
    this.group = group;
    return this;
  }

  public OtAdminOp withGroupNs(OtGroupNs groupNs) {
    this.groupNs = groupNs;
    return this;
  }

  public OtAdminOp clearKeyGroup() {
    this.keyGroup = null;
    return this;
  }

  public static OtAdminOp adminOp() {
    return new OtAdminOp();
  }

}
