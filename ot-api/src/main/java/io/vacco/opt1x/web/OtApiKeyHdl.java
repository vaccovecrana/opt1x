package io.vacco.opt1x.web;

import com.google.gson.Gson;
import io.vacco.jwt.*;
import io.vacco.murmux.http.*;
import io.vacco.opt1x.dao.OtApiKeyDao;
import io.vacco.opt1x.dto.OtUnsealOp;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.*;
import java.util.*;

import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.schema.OtConstants.*;
import static java.lang.String.format;

public class OtApiKeyHdl implements MxHandler {

  private final OtSealService sealService;
  private final OtApiKeyService keyService;
  private final MxHandler next;
  private final Gson g;

  public OtApiKeyHdl(OtSealService sealService, OtApiKeyService keyService, MxHandler next, Gson g) {
    this.sealService = Objects.requireNonNull(sealService);
    this.keyService = Objects.requireNonNull(keyService);
    this.next = Objects.requireNonNull(next);
    this.g = Objects.requireNonNull(g);
  }

  public Optional<String> apiKeyHeaderOf(MxExchange xc) {
    var hList = xc.io.getRequestHeaders().get(Opt1xKey);
    if (hList != null) {
      return hList.stream().findFirst();
    }
    return Optional.empty();
  }

  public void apiAuth(MxExchange xc, String apiKey) {
    if (sealService.isEmpty() || sealService.isSealed()) {
      var result = new OtUnsealOp().withError("Service unavailable");
      xc.withStatus(MxStatus._503)
        .withBody(MxMime.json, g.toJson(result))
        .commit();
      return;
    }
    var key = keyService.loadKey(apiKey);
    if (key.isPresent()) {
      xc.putAttachment(key.get());
      next.handle(xc);
      return;
    }
    xc.withStatus(MxStatus._401).commit();
  }

  public Optional<Jwt> validateCookie(OtCookie c) {
    try {
      if (c.jwt.verify(c.jwtRaw.value)) {
        if (new JwtValidation(c.jwt.getAlg()).validate(c.jwt) == 0) {
          return Optional.of(c.jwt);
        }
      }
      return Optional.empty();
    } catch (Exception e) {
      throw new IllegalStateException("Session cookie validation error", e);
    }
  }

  private void toLogin(MxExchange xc) {
    var requestPath = xc.getPath();
    var loginPath = format("%s?%s=%s", login, kGoto, requestPath);
    xc.withRedirect(loginPath).commit();
  }

  private void cookieAuth(MxExchange xc, OtCookie c) {
    if (validateCookie(c).isPresent()) {
      var key = g.fromJson(c.jwt.getGrant(Opt1xKey).toString(), OtApiKey.class);
      xc.putAttachment(key);
      next.handle(xc);
    } else {
      toLogin(xc);
    }
  }

  public Optional<OtCookie> cookieOf(MxExchange xc) {
    try {
      var jwtRaw = xc.cookies.get(Opt1xKey);
      if (jwtRaw != null && jwtRaw.value != null) {
        var jwt = Jwt.decode(jwtRaw.value, g::fromJson);
        jwt.setAlg(Alg, sealService.getMasterJwtKeyBytes());
        return Optional.of(OtCookie.of(jwt, jwtRaw));
      }
      return Optional.empty();
    } catch (Exception e) {
      throw new IllegalStateException("Session cookie read error", e);
    }
  }

  @Override public void handle(MxExchange xc) { // these should always be HTTPS requests, or add an option to enable/disable secure cookie
    try {
      var apiKey = apiKeyHeaderOf(xc);
      if (apiKey.isPresent()) {
        apiAuth(xc, apiKey.get());
        return;
      }
      // optional API key query param here. Insecure, but practical in trusted networks. :P
      var apiKeyParam = xc.getQueryParam(OtApiKeyDao.fld_kid);
      if (apiKeyParam != null) {
        apiAuth(xc, apiKeyParam);
        return;
      }
      var cook = cookieOf(xc);
      if (cook.isPresent()) {
        cookieAuth(xc, cook.get());
        return;
      }
      toLogin(xc); // wish there was a better way to identify API/Browser calls.
    } catch (Exception e) {
      onError("API key authentication error", e);
      toLogin(xc);
    }
  }

}
