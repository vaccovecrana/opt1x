package io.vacco.opt1x.dto;

import java.util.Objects;

public class OtValidation {

  public String name, message, key, format;

  public static OtValidation vld(String name, String message, String key, String format) {
    var v = new OtValidation();
    v.name = name;
    v.message = Objects.requireNonNull(message);
    v.key = key;
    v.format = format;
    return v;
  }

}
