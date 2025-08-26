package io.vacco.opt1x.schema;

public class OtConstants {

  public static final String Opt1x = "opt1x";

  public static final String AES = "AES";
  public static final String SHA256 = "SHA-256";
  public static final int AES_KEY_SIZE = 256;

  public static final io.vacco.jwt.Alg Alg = io.vacco.jwt.Alg.HS384;
  public static final int JwtKeySize = 384;
  public static final int UiSessionTimeoutSec = 1800; // 30 min

  public static final String Opt1xKey = "X-Opt1x-Key";
  public static final String kGoto = "goto";
  public static final String qPageSize = "pageSize";
  public static final String qNext = "next";

  public static final String
    apiRoot                       = "/api",
    apiV1Init                     = "/api/v1/init",
    apiV1Unseal                   = "/api/v1/unseal",
    apiV1Key                      = "/api/v1/key",
    apiV1Namespace                = "/api/v1/namespace",
    apiV1NamespaceId              = "/api/v1/namespace/{nsId}",
    apiV1NamespaceIdConfig        = "/api/v1/namespace/{nsId}/config",
    apiV1NamespaceIdConfigId      = "/api/v1/namespace/{nsId}/config/{cid}",
    apiV1NamespaceIdConfigIdNode  = "/api/v1/namespace/{nsId}/config/{cid}/node",
    apiV1NamespaceIdConfigIdFmt   = "/api/v1/namespace/{nsId}/config/{cid}/{fmt}",
    apiV1NamespaceIdValue         = "/api/v1/namespace/{nsId}/value",
    apiV1NamespaceKey             = "/api/v1/namespace/key",
    apiV1Value                    = "/api/v1/value";

  public static final String root = "/";
  public static final String init = "/init";
  public static final String unseal = "/unseal";
  public static final String login = "/login";

  public static final String
    ui = "/@ui",
    indexCss = "/index.css", indexCssMap = "/index.css.map", indexHtml = "/index.html",
    indexJs = "/index.js", indexJsMap = "/index.js.map",
    favicon = "/favicon.svg";

}
