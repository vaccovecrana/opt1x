package io.vacco.opt1x.context;

import com.google.gson.Gson;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.web.OtApi;
import org.codejargon.feather.Provides;

import javax.inject.Singleton;

public class OtWeb {

  @Provides @Singleton
  public OtApi otApi(OtSealService sealService, OtApiKeyService keyService,
                     OtNamespaceService nsService, OtValueService valService,
                     OtConfigService cfgService, Gson g) {
    return new OtApi(sealService, keyService, nsService, valService, cfgService, g);
  }

}
