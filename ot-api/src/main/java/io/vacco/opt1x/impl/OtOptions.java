package io.vacco.opt1x.impl;

import io.vacco.shax.logging.ShOption;
import io.vacco.shax.otel.OtHttpSink;
import org.slf4j.*;
import java.util.*;
import java.util.function.Function;

import static java.lang.Integer.parseInt;
import static java.lang.Long.parseLong;

public class OtOptions {

  public static Logger log;

  public static final String Main = "main";

  public static final String
    kJdbcUrl    = "--jdbc-url",   kDbSeed    = "--db-seed",
    kApiHost    = "--api-host",   kApiPort   = "--api-port",
    kApiSpring  = "--api-spring",
    kShares     = "--shares",     kThreshold = "--threshold",
    kLogFormat  = "--log-format", kLogLevel  = "--log-level",

    // Optional features
    kOidcIssuer       = "--oidc-issuer",
    kOidcClientId     = "--oidc-client-id",
    kOidcClientSecret = "--oidc-client-secret",
    kOidcRedirectUri  = "--oidc-redirect-uri",
    kOidcScopes       = "--oidc-scopes",

    kOtlpEndpoint     = "--otlp-endpoint",
    kOtlpTimeoutMs    = "--otlp-timeout-ms",
    kOtlpHeaders      = "--otlp-headers";

  public static OtLogFormat logFormat = OtLogFormat.text;
  public static OtLogLevel  logLevel  = OtLogLevel.info;
  public static String      host = "127.0.0.1";
  public static String      jdbcUrl;
  public static int         port = 7070;
  public static long        dbSeed = 1984;
  public static int         shares = 3, threshold = 2;
  public static boolean     apiSpring = false;

  public static String      oidcIssuer;
  public static String      oidcClientId;
  public static String      oidcClientSecret;
  public static String      oidcRedirectUri = "http://localhost:7070/oidc/callback"; // Default, overridable
  public static String      oidcScopes = "openid profile email groups"; // Default, space-separated

  public static String usage() {
    return String.join("\n",
      "Usage:",
      "  opt1x [options]",
      "Options:",
      "  --jdbc-url           [string] JDBC connection string. Required. Supports H2 (local file) or rqlite (external cluster)",
      "  --db-seed            [number] Database seed. Default: " + dbSeed,
      "  --api-host           [string] API/UI IP address. Default: " + host,
      "  --api-port           [number] API/UI port. Default: " + port,
      "  --api-spring         [flag  ] Spring Cloud Config Server support. Default " + apiSpring,
      "  --shares             [number] Number of shares. Default: " + shares,
      "  --threshold          [number] Number of shares to unseal. Default: " + threshold,
      "  --log-format         [string] Log output format ('text' or 'json'). Default: " + logFormat,
      "  --log-level          [string] Log level ('error', 'warning', 'info', 'debug', 'trace'). Default: " + logLevel,
      "  --otlp-endpoint      [string] OTEL collector URL for audit events. Enables auditing if provided.",
      "  --otlp-headers       [string] OTEL collector headers to include with each request (i.e. API keys, etc.). Default empty.",
      "  --otlp-timeout-ms    [number] OTEL collector timeout in milliseconds. Default " + OtHttpSink.TimeoutDefaultMs
      // TODO features to be implemented
      // "  --oidc-issuer        [string] OIDC issuer URL (autodiscovery endpoint). Enables OIDC if provided",
      // "  --oidc-client-id     [string] OIDC client ID. Required if OIDC enabled",
      // "  --oidc-client-secret [string] OIDC client secret. Required if OIDC enabled",
      // "  --oidc-redirect-uri  [string] OIDC redirect URI. Default: " + oidcRedirectUri,
      // "  --oidc-scopes        [string] OIDC scopes (space-separated). Default: '" + oidcScopes + "'",
    );
  }

  private static String[] argOf(String arg) {
    var sep = arg.indexOf("=");
    if (sep == -1) {
      return new String[] { arg };
    }
    var a0 = new String[2];
    a0[0] = arg.substring(0, sep);
    a0[1] = arg.substring(sep + 1);
    return a0;
  }

  private static <T> T loadProp(String v, Function<String, T> onLoad) {
    return onLoad.apply(v);
  }

  public static void setFrom(String[] args) {
    Map<String, String> argIdx = Arrays.stream(args)
      .filter(arg -> arg.startsWith("--"))
      .map(OtOptions::argOf)
      .collect(HashMap::new, (m, v) -> m.put(v[0], v.length == 1 ? null : v[1]), HashMap::putAll);

    jdbcUrl = loadProp(argIdx.get(kJdbcUrl), Function.identity());
    dbSeed = loadProp(argIdx.get(kDbSeed), v -> v != null ? parseLong(v) : dbSeed);
    host = loadProp(argIdx.get(kApiHost), v -> v != null ? v : host);
    port = loadProp(argIdx.get(kApiPort), v -> v != null ? parseInt(v) : port);
    shares = loadProp(argIdx.get(kShares), v -> v != null ? parseInt(v) : shares);
    threshold = loadProp(argIdx.get(kThreshold), v -> v != null ? parseInt(v) : threshold);
    logFormat = loadProp(argIdx.get(kLogFormat), v -> v != null ? OtLogFormat.valueOf(v) : logFormat);
    logLevel = loadProp(argIdx.get(kLogLevel), v -> v != null ? OtLogLevel.valueOf(v) : logLevel);

    apiSpring = argIdx.containsKey(kApiSpring);

    oidcIssuer = argIdx.get(kOidcIssuer);
    oidcClientId = argIdx.get(kOidcClientId);
    oidcClientSecret = argIdx.get(kOidcClientSecret);
    oidcRedirectUri = argIdx.get(kOidcRedirectUri);
    oidcScopes = argIdx.get(kOidcScopes);

    if (oidcIssuer != null) {
      if (oidcClientId == null || oidcClientSecret == null) {
        throw new IllegalArgumentException("OIDC enabled but missing required params: issuer, client-id, client-secret");
      }
    }

    if (argIdx.get(kOtlpEndpoint) != null) {
      ShOption.setSysProp(ShOption.OTEL_EXPORTER_OTLP_ENDPOINT, argIdx.get(kOtlpEndpoint));
      if (argIdx.get(kOtlpHeaders) != null) {
        ShOption.setSysProp(ShOption.OTEL_EXPORTER_OTLP_HEADERS, argIdx.get(kOtlpHeaders));
      }
      if (argIdx.get(kOtlpTimeoutMs) != null) {
        ShOption.setSysProp(ShOption.OTEL_EXPORTER_OTLP_TIMEOUT, argIdx.get(kOtlpTimeoutMs));
      }
    }
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_DEVMODE, logFormat == OtLogFormat.text ? "true" : "false");
    ShOption.setSysProp(ShOption.IO_VACCO_SHAX_LOGLEVEL, logLevel.toString());
    log = LoggerFactory.getLogger(OtOptions.class);
  }

  private static Object[] merge(Object[] args, Exception e) {
    if (args == null) {
      return new Object[] { e };
    }
    if (e != null) {
      var args0 = new Object[args.length + 1];
      System.arraycopy(args, 0, args0, 0, args.length);
      args0[args0.length - 1] = e;
      return args0;
    }
    return args;
  }

  private static String logFmt(String msgFmt, Exception e, Object[] args) {
    args[args.length - 1] = e.getMessage() != null ? e.getMessage() : e.getClass().getCanonicalName();
    msgFmt = msgFmt + " - {}";
    return msgFmt;
  }

  public static void onError(String msg, Exception e, Object ... args) {
    args = merge(args, e);
    if (log.isDebugEnabled()) {
      log.debug(msg, args);
    } else if (e != null) {
      msg = logFmt(msg, e, args);
      log.error(msg, args);
    } else {
      log.error(msg, args);
    }
  }

  public static void onWarning(String msg, Exception e, Object ... args) {
    args = merge(args, e);
    if (e != null) {
      msg = logFmt(msg, e, args);
      log.warn(msg, args);
    } else {
      log.warn(msg, args);
    }
  }

  public static String getSchema() {
    return Main;
  }

  public static boolean isRqLite() {
    return jdbcUrl.contains("rqlite");
  }

}
