package io.vacco.opt1x.impl;

import io.vacco.shax.logging.ShOption;
import org.slf4j.*;
import java.util.*;

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
    kOtelCollectorUrl = "--audit-webhook-url";

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

  public static String      otelCollectorUrl; // If set, enables OpenTelemetry auditing

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
      "  --log-level          [string] Log level ('error', 'warning', 'info', 'debug', 'trace'). Default: " + logLevel // ,
      // TODO features to be implemented
      // "  --oidc-issuer        [string] OIDC issuer URL (autodiscovery endpoint). Enables OIDC if provided",
      // "  --oidc-client-id     [string] OIDC client ID. Required if OIDC enabled",
      // "  --oidc-client-secret [string] OIDC client secret. Required if OIDC enabled",
      // "  --oidc-redirect-uri  [string] OIDC redirect URI. Default: " + oidcRedirectUri,
      // "  --oidc-scopes        [string] OIDC scopes (space-separated). Default: '" + oidcScopes + "'",
      // "  --otel-collector-url [string] OpenTelemetry Collector URL for audit events. Enables auditing if provided"
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

  public static void setFrom(String[] args) {
    Map<String, String> argIdx = Arrays.stream(args)
      .filter(arg -> arg.startsWith("--"))
      .map(OtOptions::argOf)
      .collect(HashMap::new, (m, v) -> m.put(v[0], v.length == 1 ? null : v[1]), HashMap::putAll);

    var vDbUrl = argIdx.get(kJdbcUrl);
    var vDbSeed = argIdx.get(kDbSeed);
    var vHost = argIdx.get(kApiHost);
    var vPort = argIdx.get(kApiPort);
    var vShares = argIdx.get(kShares);
    var vThreshold = argIdx.get(kThreshold);
    var vLogFormat = argIdx.get(kLogFormat);
    var vLogLevel = argIdx.get(kLogLevel);
    var vOidcIssuer = argIdx.get(kOidcIssuer);
    var vOidcClientId = argIdx.get(kOidcClientId);
    var vOidcClientSecret = argIdx.get(kOidcClientSecret);
    var vOidcRedirectUri = argIdx.get(kOidcRedirectUri);
    var vOidcScopes = argIdx.get(kOidcScopes);
    var vOtelCollectorUrl = argIdx.get(kOtelCollectorUrl);

    jdbcUrl = vDbUrl != null ? vDbUrl : jdbcUrl;
    dbSeed = vDbSeed != null ? Long.parseLong(vDbSeed) : dbSeed;
    host = vHost != null ? vHost : host;
    port = vPort != null ? Integer.parseInt(vPort) : port;
    shares = vShares != null ? Integer.parseInt(vShares) : shares;
    threshold = vThreshold != null ? Integer.parseInt(vThreshold) : threshold;
    logFormat = vLogFormat != null ? OtLogFormat.valueOf(vLogFormat) : logFormat;
    logLevel = vLogLevel != null ? OtLogLevel.valueOf(vLogLevel) : logLevel;

    apiSpring = argIdx.containsKey(kApiSpring);

    oidcIssuer = vOidcIssuer != null ? vOidcIssuer : oidcIssuer;
    oidcClientId = vOidcClientId != null ? vOidcClientId : oidcClientId;
    oidcClientSecret = vOidcClientSecret != null ? vOidcClientSecret : oidcClientSecret;
    oidcRedirectUri = vOidcRedirectUri != null ? vOidcRedirectUri : oidcRedirectUri;
    oidcScopes = vOidcScopes != null ? vOidcScopes : oidcScopes;
    otelCollectorUrl = vOtelCollectorUrl != null ? vOtelCollectorUrl : otelCollectorUrl;

    if (oidcIssuer != null) {
      if (oidcClientId == null || oidcClientSecret == null) {
        throw new IllegalArgumentException("OIDC enabled but missing required params: issuer, client-id, client-secret");
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
