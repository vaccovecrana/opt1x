package io.vacco.opt1x.schema;

public class OtConstants {

  public static String pathPrefix(String in) {
    return "/" + in;
  }

  public static final String Opt1x = "o1x";
  public static final String Opt1xRoot = pathPrefix(Opt1x);

  public static final String read = "read", write = "write";

  public static final String AES = "AES";
  public static final String SHA256 = "SHA-256";
  public static final int AES_KEY_SIZE = 256;

  public static final io.vacco.jwt.Alg Alg = io.vacco.jwt.Alg.HS384;
  public static final int JwtKeySize = 384;
  public static final int UiSessionTimeoutSec = 1800; // 30 min TODO: should this be configurable?

  public static final String Opt1xKey = "X-Opt1x-Key";
  public static final String kGoto = "goto", kFmt = "fmt";
  public static final String qPageSize = "pageSize", qNext = "next";

  public static final String
    apiRoot           = "/api",
    apiV1Init         = "/api/v1/init",
    apiV1Unseal       = "/api/v1/unseal",
    apiV1Key          = "/api/v1/key",
    apiV1Group        = "/api/v1/group",
    apiV1GroupId      = "/api/v1/group/{gid}",
    apiV1Namespace    = "/api/v1/namespace",
    apiV1NamespaceId  = "/api/v1/namespace/{nsId}",
    apiV1Value        = "/api/v1/value",
    apiV1ValueNsId    = "/api/v1/value/{nsId}",
    apiV1Config       = "/api/v1/config",
    apiV1ConfigCid    = "/api/v1/config/{cid}",
    apiV1ConfigIdFmt  = "/api/v1/config/{cid}/{fmt}";

  public static final String uiRoot   = "/";
  public static final String uiInit   = "/init";
  public static final String uiUnseal = "/unseal";
  public static final String uiLogin  = "/login";

  public static final String
    ui = "/@ui",
    indexHtml = "/index.html",
    indexCss  = "/index.css", indexCssMap = "/index.css.map",
    indexJs   = "/index.js",  indexJsMap  = "/index.js.map",
    favicon   = "/favicon.svg";

}
