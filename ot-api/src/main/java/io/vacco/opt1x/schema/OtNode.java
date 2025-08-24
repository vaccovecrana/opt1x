package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtNode {

  @MtPk public Integer nid;

  @MtFk(OtConfig.class)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtDao(loadEq = true, deleteEq = true)
  public Integer cid;

  @MtVarchar(128) @MtNotNull @MtDao
  @MtPk(idx = 1) @MtUnique(idx = 0)
  public String label;

  @MtCol @MtNotNull @MtDao
  public OtNodeType type;

  @MtFk(OtValue.class) @MtDao
  public Integer vid;

  /** Parent node id. */
  @MtFk(OtNode.class)
  public Integer pNid;

  /** Only used if node type is array item. */
  @MtCol
  @MtPk(idx = 2) @MtUnique(idx = 0)
  public Integer itemIdx;

  @Override public String toString() {
    return String.format("(%s) %s -> %d", pNid == null ? 'r' : 'c', label, vid);
  }

}
