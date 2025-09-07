package io.vacco.opt1x.schema;

import io.vacco.metolithe.core.MtCaseFormat;

public class OtSchema {

  public static final MtCaseFormat Fmt = MtCaseFormat.KEEP_CASE;

  public static final Class<?>[] schema = new Class<?>[] {
    OtApiKey.class,   OtConfig.class,
    OtGroup.class,    OtGroupNs.class,
    OtKeyGroup.class, OtNamespace.class,
    OtNode.class,     OtValue.class
  };

}
