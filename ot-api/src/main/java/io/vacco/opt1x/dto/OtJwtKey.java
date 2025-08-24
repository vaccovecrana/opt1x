package io.vacco.opt1x.dto;

import io.vacco.jwt.JwtKey;
import java.util.Objects;

public class OtJwtKey {

  public JwtKey key;
  public byte[] spec;

  public static OtJwtKey of(JwtKey key, byte[] spec) {
    var k = new OtJwtKey();
    k.key = Objects.requireNonNull(key);
    k.spec = Objects.requireNonNull(spec);
    return k;
  }

}
