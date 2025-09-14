package io.vacco.opt1x.impl;

import io.vacco.metolithe.dao.MtQuery;
import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.security.*;
import java.util.*;

import static java.lang.String.format;
import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.opt1x.impl.OtDataIO.*;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.schema.OtApiKey.rawKey;

public class OtApiKeyService {

  public  final OtDaos daos;
  private final SecureRandom random;

  public OtApiKeyService(OtDaos daos) {
    this.daos = Objects.requireNonNull(daos);
    this.random = new SecureRandom();
  }

  private MtQuery allKeysOf(String keyPath) {
    var fldPath = daos.akd.dsc.getField(OtApiKeyDao.fld_path);
    return daos.akd.query()
      .like(fldPath, keyPath + "%")
      .and()
      .neq(fldPath, keyPath);
  }

  public List<OtApiKey> allKeysOf(Integer kid) {
    var key = daos.akd.loadExisting(kid);
    return daos.akd.loadPageItems(allKeysOf(key.path));
  }

  public OtList<OtApiKey, String> loadKeysOf(Integer kid, int pageSize, String next) {
    var out = new OtList<OtApiKey, String>();
    try {
      var key = daos.akd.loadExisting(kid);
      return out.withPage(
        daos.akd.loadPage1(
          allKeysOf(key.path).limit(pageSize),
          OtApiKeyDao.fld_path, next
        )
      );
    } catch (Exception e) {
      onError("API key load error", e);
      return out.withError(e);
    }
  }

  public Optional<OtApiKey> loadRootKey() {
    return daos.akd.loadWhereNameEq(OtConstants.Opt1x).stream().findFirst();
  }

  public Optional<OtApiKey> loadKey(String key) {
    if (key == null || key.isEmpty()) {
      return Optional.empty();
    }
    var hash = sha256Of(key);
    var ok = daos.akd.loadWhereHashEq(hash).stream().findFirst();
    if (ok.isPresent()) {
      ok.get().accessUtcMs = System.currentTimeMillis();
      daos.akd.update(ok.get());
    }
    return ok;
  }

  public OtApiKeyOp validateParentApiKey(OtApiKeyOp cmd) {
    var ck = cmd.key;
    if (ck.pKid == null && ck.name.equals(Opt1x)) {
      return cmd;
    }
    var pkl = daos.akd.loadWhereKidEq(ck.pKid);
    if (pkl.isEmpty()) {
      return cmd.withError(format("Parent API key not found for child key [%s]", ck.pKid));
    }
    var pk = pkl.getFirst();
    if (pk.leaf) {
      return cmd.withError(format("Parent API key [%s] cannot create child keys", pk.name));
    }
    ck.path = format("%s/%s", pk.path, ck.name);
    return cmd;
  }

  public OtApiKeyOp duplicate(OtApiKeyOp cmd) {
    var ok0 = daos.akd.loadWhereNameEq(cmd.key.name).stream().findFirst();
    ok0.ifPresent(k0 -> {
      cmd.withError(format("API key [%s] already exists", k0.name));
      cmd.raw = null;
      cmd.key = rawKey(k0.pKid, k0.name, k0.leaf);
      cmd.key.kid = k0.kid;
    });
    return cmd;
  }

  public OtApiKeyOp newKey(OtApiKeyOp cmd) {
    var raw = String.format("%016x%016x%08x", random.nextLong(), random.nextLong(), random.nextInt());
    cmd.key.path = pathPrefix(cmd.key.name);
    cmd.key.hash = sha256Of(raw);
    cmd.key.createUtcMs = System.currentTimeMillis();
    var res = cmd.withRaw(raw);
    return OtValid.validate(cmd.key, res);
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

  /**
   * We assume that the provided key for rotation has already been
   * authenticated/unwrapped from an incoming JWT token.
   */
  public OtApiKeyOp rotate(OtApiKeyOp cmd) {
    try {
      var k0 = daos.akd.load(cmd.key.kid);
      if (k0.isEmpty()) {
        return cmd.withError(format("API key [%d] not found", cmd.key.kid));
      } else if (k0.get().name.equals(Opt1x)) {
        return cmd.withError(format("API key [%s] must not be rotated", k0.get().name));
      }
      var result = newKey(cmd).validate(this::validateParentApiKey);
      if (result.ok()) {
        daos.akd.upsert(result.key);
      }
      return cmd;
    } catch (Exception e) {
      onError("API key rotate error", e);
      return cmd.withError(e);
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
      onError("API key update error", e);
      return cmd.withError(e);
    }
  }

}
