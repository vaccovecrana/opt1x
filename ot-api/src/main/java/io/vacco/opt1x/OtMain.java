package io.vacco.opt1x;

import io.vacco.opt1x.context.*;
import io.vacco.opt1x.impl.OtOptions;
import io.vacco.opt1x.web.OtApi;
import org.codejargon.feather.Feather;

import static java.lang.String.join;

public class OtMain {

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println(OtOptions.usage());
      return;
    }

    OtOptions.jdbcUrl = "jdbc:sqlite:http://localhost:4001";
    OtOptions.logLevel = OtOptions.LogLevel.trace;
    OtOptions.setFrom(args);

    var f = Feather.with(new OtRoot(), new OtService(), new OtWeb());
    var api = f.instance(OtApi.class);
    OtOptions.log.info(
      join("\n", "",
        "               __  ____        ",
        "  ____ _______/  |/_   |__  ___",
        " /  _ \\\\____ \\   __\\   \\  \\/  /",
        "(  <_> )  |_> >  | |   |>    < ",
        " \\____/|   __/|__| |___/__/\\_ \\",
        "       |__|                  \\/"
      )
    );
    api.open();
  }

}
