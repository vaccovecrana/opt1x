package io.vacco.opt1x.impl;

import com.google.gson.Gson;
import io.vacco.jwt.JwtKeys;
import io.vacco.opt1x.shamir.Scheme;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import javax.crypto.*;
import java.security.*;
import java.util.*;

import static io.vacco.opt1x.schema.OtValue.value;
import static io.vacco.opt1x.dto.OtApiKeyOp.keyOp;
import static io.vacco.opt1x.schema.OtApiKey.rawKey;
import static io.vacco.opt1x.schema.OtConstants.*;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.impl.OtDataIO.*;

public class OtSealService {

  private final Gson            json;
  private final OtApiKeyService keyService;
  private final List<String>    shares = new ArrayList<>();
  private final Scheme          scheme;
  private final int             threshold;

  private SecretKey sealKey;
  private SecretKey masterKey;
  private OtApiKey  masterApiKey;
  private OtJwtKey  masterJwtKey;

  public OtSealService(Gson json, OtApiKeyService keyService, int totalShares, int threshold) {
    if (threshold <= 1 || totalShares > 255 || threshold > totalShares) {
      throw new IllegalArgumentException("Invalid shares/threshold combination");
    }
    var sr = new SecureRandom();
    this.json = Objects.requireNonNull(json);
    this.scheme = new Scheme(sr, totalShares, threshold);
    this.threshold = threshold;
    this.keyService = Objects.requireNonNull(keyService);
    this.keyService.loadRootKey().ifPresent(mk -> this.masterApiKey = mk);
  }

  public Map<Integer, byte[]> seal() {
    var skB64 = Base64.getEncoder().encodeToString(sealKey.getEncoded());
    return scheme.split(skB64.getBytes());
  }

  public String[] sealB64() {
    var shares = seal();
    return shares.entrySet().stream()
      .map(e -> String.format(
        "%d:%s",
        e.getKey(), Base64.getEncoder().encodeToString(e.getValue()))
      )
      .toArray(String[]::new);
  }

  public OtInitOp initialize() {
    var result = new OtInitOp();
    try {
      if (masterApiKey != null) {
        return result.withError("Opt1x already initialized");
      }
      var kr = keyService.createApiKey(keyOp().withKey(
        rawKey(null, Opt1x, false)
      ));
      var jk = newJwtKey();

      this.sealKey = newAesKey();
      this.masterKey = newAesKey();
      this.masterApiKey = kr.key;
      this.masterJwtKey = OtJwtKey.of(jk, JwtKeys.getHMACKeyFromB64(jk.keyB64, Alg).getEncoded());

      var m2 = Base64.getEncoder().encodeToString(this.masterKey.getEncoded());
      var m3 = json.toJson(this.masterJwtKey);

      this.masterApiKey.metadata2 = encryptRaw(m2, sealKey);
      this.masterApiKey.metadata3 = encryptRaw(m3, sealKey);
      this.masterApiKey.createUtcMs = System.currentTimeMillis();

      var cmd = keyService.update(keyOp().withKey(this.masterApiKey));
      if (!cmd.ok()) {
        return result.setFrom(cmd);
      }

      result.rootApiKey = kr.raw;
      result.shares = sealB64();

      var now = System.currentTimeMillis();

      var rootGroup = keyService.daos.grd.upsert(OtGroup.group(null, Opt1x, Opt1xRoot, now)).rec;
      var rootNs = keyService.daos.nsd.upsert(OtNamespace.namespace(null, Opt1x, Opt1xRoot, now)).rec;
      var rootGns = OtGroupNs.groupNs(rootGroup.gid, rootNs.nsId, masterApiKey.kid, true, true, true);
      var rootKg = OtKeyGroup.keyGroup(masterApiKey.kid, rootGroup.gid, OtGroupRole.Admin, masterApiKey.kid);

      rootGns.grantUtcMs = now;
      rootKg.grantUtcMs = now;
      keyService.daos.gnd.upsert(rootGns);
      keyService.daos.kgd.upsert(rootKg);

      this.sealKey = null;
      this.masterKey = null;
      this.masterJwtKey = null;
    } catch (Exception e) {
      onError("Failed to initialize Opt1x", e);
      result.error = e.getMessage();
    }
    return result;
  }

  public void unseal(Map<Integer, byte[]> shares) {
    var skB64 = new String(scheme.join(shares));
    this.sealKey = loadAesKey(skB64);
    this.masterKey = loadAesKey(decryptRaw(this.masterApiKey.metadata2, this.sealKey));
    this.masterJwtKey = json.fromJson(decryptRaw(this.masterApiKey.metadata3, sealKey), OtJwtKey.class);
    this.sealKey = null;
  }

  public void unsealB64(String ... sharesB64) {
    var shares = new LinkedHashMap<Integer, byte[]>();
    for (var s : sharesB64) {
      var parts = s.split(":");
      shares.put(Integer.parseInt(parts[0]), Base64.getDecoder().decode(parts[1]));
    }
    unseal(shares);
  }

  public OtUnsealOp loadUnsealKey(String unsealKey) {
    var result = new OtUnsealOp();
    try {
      if (isEmpty()) {
        throw new IllegalStateException("Opt1x not initialized");
      }
      if (!isSealed()) {
        throw new IllegalStateException("Opt1x already unsealed");
      }
      if (unsealKey == null || unsealKey.isEmpty()) {
        throw new IllegalArgumentException("Unseal key cannot be null or empty");
      }
      shares.add(unsealKey);
      var ready = shares.size() >= threshold;
      if (ready) {
        unsealB64(shares.toArray(new String[0]));
        shares.clear();
      }
      return result.set(shares.size(), ready);
    } catch (Exception e) {
      onError("Failed to load unseal key", e);
      shares.clear();
      result.error = e.getMessage();
      result.loadedKeys = 0;
    }
    return result;
  }

  private void checkMasterKey() {
    if (masterKey == null) {
      throw new IllegalStateException("System is sealed. Unseal first.");
    }
  }

  public SecretKey getMasterKey() {
    checkMasterKey();
    return masterKey;
  }

  public OtValue encrypt(OtValue val) {
    checkMasterKey();
    if (val.encrypted) {
      val.val = encryptRaw(val.val, masterKey);
    }
    return val;
  }

  public OtValue decrypt(OtValue val) {
    checkMasterKey();
    if (!val.encrypted) {
      return val;
    }
    var dec = value(
      val.nsId, val.name, decryptRaw(val.val, masterKey),
      val.type, val.notes, false
    );
    dec.createUtcMs = val.createUtcMs;
    return dec;
  }

  public boolean isEmpty() {
    return masterApiKey == null;
  }

  public boolean isSealed() {
    return masterKey == null;
  }

  public byte[] getMasterJwtKeyBytes() {
    return masterJwtKey.spec;
  }

  // TODO implement data export method to re-encrypt secrets in a new Opt1x instance.

}
