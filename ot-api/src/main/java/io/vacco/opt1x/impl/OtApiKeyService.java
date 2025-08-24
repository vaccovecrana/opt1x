package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.OtApiKeyDao;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.security.*;
import java.util.*;

import static java.lang.String.format;
import static io.vacco.opt1x.impl.OtDataIO.*;
import static io.vacco.opt1x.impl.OtOptions.onError;

public class OtApiKeyService {

  public  final OtDaos daos;
  private final SecureRandom random;

  public OtApiKeyService(OtDaos daos) {
    this.daos = Objects.requireNonNull(daos);
    this.random = new SecureRandom();
  }

  public OtList<OtApiKey, String> loadKeysOf(Integer kid, int pageSize, String next) {
    var out = new OtList<OtApiKey, String>();
    try {
      var key = daos.akd.loadExisting(kid);
      var fldPath = daos.akd.dsc.getField(OtApiKeyDao.fld_path);
      return out.withPage(
        daos.akd.loadPage1(
          pageSize, false,
          daos.akd.query()
            .like(fldPath, key.path + "%")
            .and()
            .neq(fldPath, key.path),
          OtApiKeyDao.fld_path, next
        )
      );
    } catch (Exception e) {
      onError("API key load error", e);
      return out.withError(e);
    }
  }

  public Optional<OtApiKey> loadRootKey() {
    return daos.akd.loadWhereNameEq(OtConstants.Opt1x)
      .stream()
      .filter(OtDataIO::isActiveKey)
      .filter(k -> OtRole.Admin.equals(k.role))
      .findFirst();
  }

  public Optional<OtApiKey> loadKey(String keyHash) {
    if (keyHash == null || keyHash.isEmpty()) {
      return Optional.empty();
    }
    var hash = sha256Of(keyHash);
    return daos.akd.loadWhereHashEq(hash)
      .stream()
      .filter(OtDataIO::isActiveKey)
      .findFirst();
  }

  public OtApiKeyOp validateParentApiKey(OtApiKeyOp cmd) {
    var childKey = cmd.key;
    if (childKey.pKid == null && childKey.role == OtRole.Admin) {
      return cmd;
    }
    var parentKeys = daos.akd.loadWhereKidEq(childKey.pKid);
    if (parentKeys.isEmpty()) {
      return cmd.withError(format("Parent API key not found for child key [%s]", childKey.pKid));
    }
    var parentKey = parentKeys.getFirst();
    if (!isActiveKey(parentKey)) {
      return cmd.withError(format("Parent API key [%s] is not active", parentKey.name));
    }
    if (parentKey.role != OtRole.Admin && childKey.pKid != null) {
      return cmd.withError(format(
        "Parent API key [%s] cannot create child API key [%s] with role [%s]",
        parentKey.name, childKey.name, childKey.role
      ));
    }
    childKey.path = format("%s%s", parentKey.path, childKey.path);
    return cmd;
  }

  public OtApiKeyOp duplicate(OtApiKeyOp cmd) {
    var ok0 = daos.akd.loadWhereNameEq(cmd.key.name).stream().findFirst();
    ok0.ifPresent(k0 -> {
      cmd.withError(format("API key [%s] already exists", k0.name));
      cmd.raw = null;
      cmd.key = k0; // TODO security: just provide enough detail about the existing key (no hash, no path, etc.).
    });
    return cmd;
  }

  public OtApiKeyOp newKey(OtApiKeyOp cmd) {
    var val0 = Long.toHexString(random.nextLong());
    var val1 = Long.toHexString(random.nextLong());
    var raw = format("%s%s", val0, val1);
    var key = OtApiKey.of(cmd.parentKid, cmd.name, sha256Of(raw), cmd.role, System.currentTimeMillis());
    var res = cmd.withKey(key).withRaw(raw);
    return OtValid.validate(key, res);
  }

  public OtApiKeyOp createApiKey(OtApiKeyOp cmd) {
    try {
      var result = newKey(cmd)
        .validate(this::duplicate)
        .validate(this::validateParentApiKey);
      if (result.ok()) {
        daos.akd.upsert(result.key);
      }
      return result;
    } catch (Exception e) {
      onError("API key create error", e);
      return new OtApiKeyOp().withError(e);
    }
  }

  public OtApiKeyOp update(OtApiKeyOp cmd) {
    try {
      cmd = validateParentApiKey(cmd);
      if (cmd.ok()) {
        cmd.key = daos.akd.update(cmd.key).rec;
      }
      return cmd;
    } catch (Exception e) {
      onError("API key metadata write error", e);
      return cmd.withError(e);
    }
  }

}
