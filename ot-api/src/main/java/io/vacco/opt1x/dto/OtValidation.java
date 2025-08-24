package io.vacco.opt1x.dto;

import java.util.Objects;

public class OtValidation {

  public String name, message, key, format;

  public static OtValidation of(String name, String message, String key, String format) {
    var v = new OtValidation();
    v.name = Objects.requireNonNull(name);
    v.message = Objects.requireNonNull(message);
    v.key = Objects.requireNonNull(key);
    v.format = format;
    return v;
  }

}
