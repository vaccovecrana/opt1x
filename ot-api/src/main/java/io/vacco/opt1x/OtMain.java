package io.vacco.opt1x;

import io.vacco.opt1x.context.*;
import io.vacco.opt1x.impl.OtOptions;
import io.vacco.opt1x.web.OtApi;
import org.codejargon.feather.Feather;
import java.nio.charset.StandardCharsets;
import java.util.*;

import static java.lang.String.join;

public class OtMain {

  public static String loadVersion() {
    try (var is = OtMain.class.getResourceAsStream("/ui/version");
         var scanner = new Scanner(Objects.requireNonNull(is), StandardCharsets.UTF_8)) {
      return scanner.useDelimiter("\\A").next();
    } catch (Exception e) {
      return "[dev]";
    }
  }

  public static void main(String[] args) {
    if (args == null || args.length == 0) {
      System.out.println(OtOptions.usage());
      return;
    }
    var t0 = System.currentTimeMillis();
    var ver = loadVersion();
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
        "       |__|                  \\/",
        String.format(
          "Opt1x %s started in %dms",
          ver, System.currentTimeMillis() - t0
        )
      )
    );
    api.open();
  }

}
