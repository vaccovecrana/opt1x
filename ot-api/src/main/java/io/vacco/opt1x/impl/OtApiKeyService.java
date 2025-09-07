package io.vacco.opt1x.impl;

import io.vacco.metolithe.dao.MtQuery;
import io.vacco.opt1x.dao.OtApiKeyDao;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.security.*;
import java.util.*;

import static java.lang.String.format;
import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.opt1x.impl.OtDataIO.*;
import static io.vacco.opt1x.impl.OtOptions.onError;

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

  public Optional<OtApiKey> loadKey(String keyHash) {
    if (keyHash == null || keyHash.isEmpty()) {
      return Optional.empty();
    }
    var hash = sha256Of(keyHash);
    return daos.akd.loadWhereHashEq(hash)
      .stream()
      .findFirst();
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
      cmd.key = k0; // TODO security: just provide enough detail about the existing key (no hash, no path, no metadata, etc.).
    });
    return cmd;
  }

  public OtApiKeyOp newKey(OtApiKeyOp cmd) {
    var raw = format("%s%s", Long.toHexString(random.nextLong()), Long.toHexString(random.nextLong()));
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
