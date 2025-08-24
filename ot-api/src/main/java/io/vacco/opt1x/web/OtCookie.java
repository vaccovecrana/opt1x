package io.vacco.opt1x.web;

import io.vacco.jwt.Jwt;
import io.vacco.murmux.http.MxCookie;
import java.util.Objects;

public class OtCookie {

  public Jwt jwt;
  public MxCookie jwtRaw;

  public static OtCookie of(Jwt jwt, MxCookie jwtRaw) {
    var cookie = new OtCookie();
    cookie.jwtRaw = Objects.requireNonNull(jwtRaw);
    cookie.jwt = Objects.requireNonNull(jwt);
    return cookie;
  }

}
