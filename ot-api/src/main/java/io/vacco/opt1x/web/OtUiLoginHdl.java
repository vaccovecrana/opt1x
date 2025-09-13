package io.vacco.opt1x.web;

import com.google.gson.Gson;
import io.vacco.jwt.Jwt;
import io.vacco.murmux.http.*;
import io.vacco.opt1x.impl.*;
import java.util.Objects;

import static io.vacco.jwt.Jwt.*;
import static io.vacco.opt1x.schema.OtConstants.*;
import static java.lang.String.format;

public class OtUiLoginHdl implements MxHandler {

  private final OtApiKeyService keyService;
  private final OtSealService   sealService;
  private final Gson            g;

  public OtUiLoginHdl(OtApiKeyService keyService,
                      OtSealService sealService, Gson g) {
    this.keyService = Objects.requireNonNull(keyService);
    this.sealService = Objects.requireNonNull(sealService);
    this.g = Objects.requireNonNull(g);
  }

  @Override public void handle(MxExchange xc) {
    try {
      var goTo = xc.getQueryParam(kGoto);
      var keyRaw = Objects.requireNonNull(xc.formParams.get(Opt1xKey));
      var key = keyService.loadKey(keyRaw);
      if (key.isPresent()) {
        var tk = new Jwt();
        tk.setAlg(Alg, sealService.getMasterJwtKeyBytes());
        tk.addGrant(Opt1xKey, g.toJson(key.get()));
        tk.addGrant(kExp, tk.nowPlus(UiSessionTimeoutSec));
        xc
          .withCookie(new MxCookie(Opt1xKey, tk.encode(g::toJson))) // TODO configure mark secure cookie
          .withRedirect(goTo != null ? goTo : uiLogin)
          .commit();
      } else {
        xc
          .withStatus(MxStatus._401)
          .withRedirect(format("%s?%s=%s&failed=true", uiLogin, kGoto, goTo))
          .commit();
      }
    } catch (Exception e) {
      throw new IllegalStateException("Login error", e);
    }
  }

}
