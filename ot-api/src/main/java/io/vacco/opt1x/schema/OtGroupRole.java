package io.vacco.opt1x.schema;

public enum OtGroupRole {

  Member, Admin;

  public boolean includes(OtGroupRole role) {
    return this != Member || role != Admin;
  }

}
