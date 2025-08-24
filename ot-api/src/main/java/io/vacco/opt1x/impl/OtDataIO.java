package io.vacco.opt1x.impl;

import io.vacco.jwt.*;
import io.vacco.opt1x.schema.OtApiKey;
import javax.crypto.*;
import java.nio.charset.StandardCharsets;
import java.security.*;
import java.util.Base64;

import static io.vacco.opt1x.schema.OtConstants.*;
import static java.lang.String.format;

public class OtDataIO {

  public static String sha256Of(String keyValue) {
    try {
      var digest = MessageDigest.getInstance(SHA256);
      var hash = digest.digest(keyValue.getBytes(StandardCharsets.UTF_8));
      var hexString = new StringBuilder();
      for (byte b : hash) {
        var hex = Integer.toHexString(0xff & b);
        if (hex.length() == 1) hexString.append('0');
        hexString.append(hex);
      }
      return hexString.toString();
    } catch (NoSuchAlgorithmException e) {
      throw new IllegalStateException("SHA-256 not available", e);
    }
  }

  public static String encryptRaw(String value, SecretKey aesKey) {
    try {
      var cipher = Cipher.getInstance(AES);
      cipher.init(Cipher.ENCRYPT_MODE, aesKey);
      return Base64.getEncoder().encodeToString(cipher.doFinal(value.getBytes()));
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static String decryptRaw(String encryptedB64, SecretKey aesKey) {
    try {
      var cipher = Cipher.getInstance(AES);
      cipher.init(Cipher.DECRYPT_MODE, aesKey);
      var decrypted = cipher.doFinal(Base64.getDecoder().decode(encryptedB64));
      return new String(decrypted);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  public static SecretKey loadAesKey(String keyB64) {
    return new javax.crypto.spec.SecretKeySpec(Base64.getDecoder().decode(keyB64), AES);
  }

  public static SecretKey newAesKey() throws NoSuchAlgorithmException {
    var keyGen = KeyGenerator.getInstance(AES);
    keyGen.init(AES_KEY_SIZE, new SecureRandom());
    return keyGen.generateKey();
  }

  public static JwtKey newJwtKey() {
    return JwtKeys.generateKey(Alg, JwtKeySize);
  }

  public static boolean isActiveKey(OtApiKey key) {
    return key != null && key.deletedAtUtcMs == 0;
  }

}
