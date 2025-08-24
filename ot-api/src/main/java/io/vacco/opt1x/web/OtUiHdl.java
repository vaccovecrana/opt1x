package io.vacco.opt1x.web;

import io.vacco.murmux.http.*;
import io.vacco.murmux.middleware.MxStatic;
import java.io.File;
import java.nio.file.*;

import static io.vacco.opt1x.impl.OtOptions.log;
import static java.util.Objects.requireNonNull;
import static io.vacco.opt1x.schema.OtConstants.*;

public class OtUiHdl extends MxStatic {

  private static final File projectRoot = resolveCommonPath(new File("."), "opt1x");
  private static final File pkgJson = projectRoot != null && projectRoot.exists()
    ? new File(projectRoot, "./ot-ui/package.json")
    : null;

  private static final Origin origin = pkgJson != null ? Origin.FileSystem : Origin.Classpath;
  private static final Path root = pkgJson != null
   ? Paths.get(requireNonNull(projectRoot).toPath().toString(), "./ot-ui/build/resources/main/ui")
   : Paths.get("/ui");

  @SuppressWarnings("this-escape")
  public OtUiHdl() {
    super(origin, root);
    log.info("Resource origin: {}, root: {}", origin, root);
    this.withNoTypeResolver((p, o) -> p.endsWith(".map") ? MxMime.json.type : MxMime.bin.type);
  }

  @Override public void handle(MxExchange xc) {
    var p = xc.getPath();
    if (p.startsWith(ui)) {
      super.handle(xc);
      return;
    }
    switch (p) {
      case indexCss:
      case indexCssMap:
      case indexJs:
      case indexJsMap:
      case favicon:
        super.handle(xc);
        break;
      default: // any other Preact router path
        super.handleWithPath(xc, indexHtml);
    }
  }

}
