package io.vacco.opt1x.schema;

import io.vacco.metolithe.core.MtCaseFormat;

public class OtSchema {

  public static final MtCaseFormat Fmt = MtCaseFormat.KEEP_CASE;

  public static final Class<?>[] schema = new Class<?>[] {
    OtApiKey.class, OtKeyNamespace.class,
    OtNamespace.class, OtValue.class,
    OtConfig.class, OtNode.class
  };

}
