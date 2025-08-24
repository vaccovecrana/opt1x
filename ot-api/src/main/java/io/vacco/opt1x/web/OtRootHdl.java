package io.vacco.opt1x.web;

import com.google.gson.Gson;
import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.*;
import io.vacco.opt1x.impl.*;
import io.vacco.ronove.murmux.RvMxAdapter;
import java.util.function.*;

import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.schema.OtConstants.*;

public class OtRootHdl extends MxRouter {

  public static MxHandler initOr(OtSealService ss, MxHandler next) {
    return xc -> {
      if (ss.isEmpty()) {
        next.handle(xc);
      } else if (ss.isSealed()) {
        xc.withRedirect(unseal).commit();
      } else {
        xc.withRedirect(root).commit();
      }
    };
  }

  public static MxHandler unsealOr(OtSealService ss, MxHandler next) {
    return xc -> {
      if (ss.isEmpty()) {
        xc.withRedirect(init).commit();
      } else if (ss.isSealed()) {
        next.handle(xc);
      } else {
        xc.withRedirect(root).commit();
      }
    };
  }

  // TODO fix: go to this URL after colb boot (sealed): http://127.0.0.1:7070/login?goto=/namespaces/835482165/config/-1451385404
  public static MxHandler sysCheckOr(OtSealService ss, OtApiKeyHdl keyApiHdl, boolean goToRoot, MxHandler next) {
    return xc -> {
      if (ss.isEmpty()) {
        xc.withRedirect(init).commit();
      } else if (ss.isSealed()) {
        xc.withRedirect(unseal).commit();
      } else {
        var cookieOk = keyApiHdl
          .cookieOf(xc)
          .flatMap(keyApiHdl::validateCookie)
          .isPresent();
        if (cookieOk) {
          if (goToRoot) {
            xc.withRedirect(root).commit();
          } else {
            next.handle(xc);
          }
        } else {
          next.handle(xc);
        }
      }
    };
  }

  @SuppressWarnings("this-escape")
  public OtRootHdl(OtSealService ss, OtApiKeyService ks, OtUiHdl uiHdl, OtApiHdl apiHdl, Gson g) {
    var errorHdl = (BiConsumer<MxExchange, Exception>) (xc, e) -> {
      xc.putAttachment(e);
      onError("Unhandled exception: {}", e, xc.io.getRequestURI());
    };
    var apiAdpHdl = new RvMxAdapter<>(apiHdl, errorHdl, g::fromJson, g::toJson).build();
    var keyApiHdl = new OtApiKeyHdl(ss, ks, apiAdpHdl, g);
    var keyUiHdl = new OtApiKeyHdl(ss, ks, uiHdl, g);
    var loginHdl = new OtUiLoginHdl(ks, ss, g);

    // UI static resources
    get(indexCss, uiHdl);
    get(indexCssMap, uiHdl);
    get(indexJs, uiHdl);
    get(indexJsMap, uiHdl);
    get(favicon, uiHdl);
    prefix(ui, uiHdl);

    // Login actions
    get(login, sysCheckOr(ss, keyApiHdl, true, uiHdl));
    post(login, loginHdl);

    // Init/Unseal action
    get(init, initOr(ss, uiHdl));
    get(unseal, unsealOr(ss, uiHdl));

    // API actions
    any(apiV1Init, apiAdpHdl);
    any(apiV1Unseal, apiAdpHdl);
    prefix(apiRoot, keyApiHdl);

    // Authenticated preact UI routes
    noMatch(sysCheckOr(ss, keyApiHdl, false, keyUiHdl));
  }

}
