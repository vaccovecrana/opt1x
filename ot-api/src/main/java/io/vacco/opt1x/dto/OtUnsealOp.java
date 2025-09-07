package io.vacco.opt1x.dto;

public class OtUnsealOp extends OtResult {

  public int     loadedKeys;
  public boolean ready;

  public OtUnsealOp set(int loadedKeys, boolean ready) {
    this.loadedKeys = loadedKeys;
    this.ready = ready;
    return this;
  }

}
