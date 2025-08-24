package io.vacco.opt1x.context;

import com.google.gson.Gson;
import org.codejargon.feather.Provides;
import javax.inject.Singleton;

public class OtRoot {

  @Provides @Singleton
  public Gson gson() {
    return new Gson();
  }

}
