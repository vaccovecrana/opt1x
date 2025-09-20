package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import org.slf4j.*;
import java.util.Map;

import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.shax.logging.ShArgument.kv;

public class OtAudit {

  private static final Logger log = LoggerFactory.getLogger(OtAudit.class);

  private static String pfx(String v) {
    return String.format("%s.%s", Opt1xL, v);
  }

  public static String configName() {
    return OtConfigDao.fld_cid + OtConfigDao.fld_name;
  }

  public static String grantKeyName() {
    return OtGroupNsDao.fld_grantKid + OtApiKeyDao.fld_name;
  }

  public static String parentKeyName() {
    return OtApiKeyDao.fld_pKid + OtApiKeyDao.fld_name;
  }

  public static String keyName() {
    return OtApiKeyDao.fld_kid + OtApiKeyDao.fld_name;
  }

  public static String valueName() {
    return OtValueDao.fld_val + OtValueDao.fld_name;
  }

  public static String groupPath() {
    return OtGroupDao.fld_gid + OtGroupDao.fld_path;
  }

  public static String nsPath() {
    return OtNamespaceDao.fld_nsId + OtNamespaceDao.fld_path;
  }

  public static String valN(int placeholder) {
    return OtValueDao.fld_val + placeholder;
  }

  public static String getCurrentMethodName() {
    var methodName = StackWalker.getInstance()
      .walk(
        stream -> stream.skip(1).findFirst()
          .map(StackWalker.StackFrame::getMethodName)
      );
    return methodName.orElse("unknown");
  }

  public static void noNsAccess(OtApiKey key, String nsPath, String type) {
    log.warn(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(OtNamespaceDao.fld_path), nsPath),
      kv(pfx(OtValueDao.fld_type), type)
    );
  }

  public static void createNamespace(OtApiKey key, OtNamespace ns) {
    log.info(
      "{} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(nsPath()), ns.path)
    );
  }

  public static void createGroup(OtApiKey key, OtGroup g) {
    log.info(
      "{} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(groupPath()), g.path)
    );
  }

  public static void deleteGroup(OtApiKey key, OtGroup g) {
    log.info(
      "{} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(groupPath()), g.path)
    );
  }

  public static void bindGroupToNamespace(OtApiKey key, String groupPath, String nsPath) {
    log.info(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(groupPath()), groupPath),
      kv(pfx(nsPath()), nsPath)
    );
  }

  public static void bindKeyToGroup(String grantKeyName, String keyName, String groupPath) {
    log.info(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(grantKeyName()), grantKeyName),
      kv(pfx(keyName()), keyName),
      kv(pfx(groupPath()), groupPath)
    );
  }

  public static void createApiKey(String parentKeyName, String keyName) {
    if (parentKeyName != null) {
      log.info(
        "{} {} {}",
        kv(pfx(kEvent), getCurrentMethodName()),
        kv(pfx(parentKeyName()), parentKeyName),
        kv(pfx(keyName()), keyName)
      );
    } else {
      log.info(
        "{} {}",
        kv(pfx(kEvent), getCurrentMethodName()),
        kv(pfx(keyName()), keyName)
      );
    }
  }

  public static void rotateApiKey(OtApiKey key) {
    log.info(
      "{} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name)
    );
  }

  public static void updateApiKey(OtApiKey key) {
    log.info(
      "{} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), key.name),
      kv(pfx(OtApiKey.class.getSimpleName()), key)
    );
  }

  public static void createConfig(String keyName, String cfgName, String nsPath) {
    log.info(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(configName()), cfgName),
      kv(pfx(nsPath()), nsPath)
    );
  }

  public static void updateConfig(String keyName, String cfgName, String nsPath, Map<String, OtVar> treeIdx) {
    log.info(
      "{} {} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(configName()), cfgName),
      kv(pfx(nsPath()), nsPath),
      kv(pfx(OtConfig.class.getSimpleName()), treeIdx)
    );
  }

  public static void loadConfig(String keyName, String cfgName, String nsPath, OtRequest req) {
    log.info(
      "{} {} {} {} {} {} {} {} {} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(configName()), cfgName),
      kv(pfx(nsPath()), nsPath),
      kv(pfx("remoteAddress"), req.remoteAddress),
      kv(pfx("remoteHost"),    req.remoteHost),
      kv(pfx("remotePort"),    req.remotePort),
      kv(pfx("userAgent"),     req.userAgent),
      kv(pfx("host"),          req.host),
      kv(pfx("protocol"),      req.protocol),
      kv(pfx("headers"),       req.headers),
      kv(pfx("queryParams"),   req.queryParams)
    );
  }

  public static void cloneConfig(String keyName, String cfgName, String nsPath) {
    log.info(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(configName()), cfgName),
      kv(pfx(nsPath()), nsPath)
    );
  }

  public static void upsertValue(String keyName, String valName, String val, String nsPath) {
    log.info(
      "{} {} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(valueName()), valName),
      kv(pfx(valN(0)), val),
      kv(pfx(nsPath()), nsPath)
    );
  }

  public static void restoreValueVersion(String keyName, String valName, String v0, String v1, String nsPath) {
    log.info(
      "{} {} {} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(valueName()), valName),
      kv(pfx(valN(0)), v0),
      kv(pfx(valN(1)), v1),
      kv(pfx(nsPath()), nsPath)
    );
  }

  public static void deleteValueVersion(String keyName, String valName, String verVal, String nsPath) {
    log.info(
      "{} {} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(valueName(), valName),
      kv(valN(0), verVal)
    );
  }

  public static void deleteValue(String keyName, String valName) {
    log.info(
      "{} {} {}",
      kv(pfx(kEvent), getCurrentMethodName()),
      kv(pfx(keyName()), keyName),
      kv(pfx(valueName()), valName)
    );
  }

}
