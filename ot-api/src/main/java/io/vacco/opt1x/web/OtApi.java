package io.vacco.opt1x.web;

import com.google.gson.Gson;
import io.vacco.murmux.Murmux;
import io.vacco.opt1x.impl.*;
import java.util.concurrent.Executors;

import static io.vacco.opt1x.impl.OtOptions.log;
import static java.lang.String.format;

public class OtApi {

  private final Murmux mx;

  public OtApi(OtSealService sealService, OtApiKeyService keyService,
               OtNamespaceService nsService, OtValueService valService,
               OtConfigService cfgService, Gson g) {
    var x = Executors.newCachedThreadPool(r -> new Thread(r, format("ot-api-%x", r.hashCode())));
    this.mx = new Murmux(OtOptions.host, x);
    var uiHdl = new OtUiHdl();
    var apiHdl = new OtApiHdl(sealService, keyService, nsService, valService, cfgService);
    var rootHdl = new OtRootHdl(sealService, keyService, uiHdl, apiHdl, g);
    mx.rootHandler(rootHdl);
  }

  public OtApi open() {
    mx.listen(OtOptions.port);
    log.info("ui - ready at http://{}:{}", OtOptions.host, OtOptions.port);
    return this;
  }

}
