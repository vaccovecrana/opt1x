package io.vacco.opt1x.schema;

import io.vacco.metolithe.annotations.*;

@MtEntity public class OtValue {

  @MtPk
  @MtDao(loadIn = true)
  public Integer vid;

  @MtFk(OtNamespace.class)
  @MtPk(idx = 0) @MtUnique(idx = 0)
  @MtDao(loadEq = true, listIn = true) @MtNotNull
  public Integer nsId;

  @MtVarchar(128) @MtNotNull
  @MtPk(idx = 1) @MtUnique(idx = 0)
  @MtDao(loadEq = true)
  public String name;

  @MtVarchar(4096) @MtNotNull @MtDao
  public String val;

  @MtCol @MtNotNull @MtDao
  public OtValueType type;

  @MtVarchar(4096)
  public String notes;

  @MtCol @MtDao
  public boolean encrypted;

  @MtCol @MtDao
  public long createUtcMs;

  public static OtValue value(Integer nsId, String name, String value,
                              OtValueType type, String notes, boolean encrypted) {
    var ov = new OtValue();
    ov.nsId = nsId;
    ov.name = name;
    ov.val = value;
    ov.type = type;
    ov.notes = notes;
    ov.encrypted = encrypted;
    return ov;
  }

  @Override public String toString() {
    return String.format(
        "%d, %d, %s, %s, %b",
        vid, nsId, name, val, encrypted
    );
  }

  @Override public boolean equals(Object obj) {
    return obj instanceof OtValue
      && ((OtValue) obj).val != null
      && ((OtValue) obj).val.equals(this.val);
  }

  @Override public int hashCode() {
    return this.val == null ? 0 : this.val.hashCode();
  }

}
