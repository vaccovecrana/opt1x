package io.vacco.opt1x;

import com.google.gson.Gson;
import com.zaxxer.hikari.HikariDataSource;
import io.vacco.opt1x.context.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.*;
import j8spec.annotation.DefinedOrder;
import j8spec.junit.J8SpecRunner;
import org.codejargon.feather.Feather;
import org.junit.runner.RunWith;
import java.io.*;

import static j8spec.J8Spec.*;
import static java.lang.String.format;
import static org.junit.Assert.*;
import static io.vacco.opt1x.dto.OtApiKeyOp.keyCmd;
import static io.vacco.opt1x.dto.OtNamespaceOp.nsCreate;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtServiceTest {
  static {
    OtOptions.setFrom(new String[] {
      format("%s=%s", OtOptions.kJdbcUrl,  "jdbc:hsqldb:file:./build/db-test/opt1x"),
      format("%s=%s", OtOptions.kLogLevel, "trace")
    });
  }

  private static final Feather f = Feather.with(new OtRoot(), new OtService());
  private static String[] shares;

  static {
    it("Initializes a seal service", () -> {
      var ss = f.instance(OtSealService.class);
      var g = f.instance(Gson.class);
      var sharesFile = new File("./build/shares.json");

      if (ss.isEmpty()) {
        var initOp = ss.initialize();
        assertTrue(initOp.ok());
        shares = initOp.shares;
        OtOptions.log.info("Shares generated: {}", shares.length);
        try (var w = new FileWriter(sharesFile)) {
          g.toJson(shares, w);
        }
      } else if (sharesFile.exists()) {
        try (var r = new FileReader(sharesFile)) {
          shares = g.fromJson(r, String[].class);
        }
      }

      ss.unsealB64(shares[0], shares[2]);
    });

    it("Lets an API key create child API keys only if it has permissions to do so", () -> {
      var ks = f.instance(OtApiKeyService.class);

      var adminKeyRes = ks.createApiKey(keyCmd(null, "admin-key-test", OtRole.Admin));
      assertNotNull(adminKeyRes.key.kid);
      assertEquals(OtRole.Admin, adminKeyRes.key.role);

      var appKeyName = "app-key-test";
      var result = ks.createApiKey(keyCmd(adminKeyRes.key.kid, appKeyName, OtRole.Application));
      assertNotNull(result.key);

      var appKey = result.key;
      assertNotNull(appKey.kid);
      assertEquals(adminKeyRes.key.kid, appKey.pKid);
      assertEquals(OtRole.Application, appKey.role);

      result = ks.createApiKey(keyCmd(appKey.kid, "child-key-test", OtRole.Application));
      assertNotNull(result.error);

      var res0 = ks.loadKeysOf(adminKeyRes.key.kid, 10, null);
      assertEquals(1, res0.page.size);
      assertEquals(appKeyName, res0.page.items.getFirst().name);

      var res1 = ks.loadKeysOf(appKey.kid, 10, null);
      assertFalse(res1.page.hasNext());
    });

    it("Allows API keys to manage namespaces", () -> {
      var ks = f.instance(OtApiKeyService.class);
      var ns = f.instance(OtNamespaceService.class);

      // Steve's admin key for team-a
      var steveRes = ks.createApiKey(keyCmd(null, "steve-admin-key", OtRole.Admin));
      var steveKey = steveRes.key;
      assertTrue(steveRes.ok() || steveRes.error.contains("already"));

      // Create team-a root namespace
      var teamARoot = ns.createNamespace(nsCreate(steveKey.kid, "team-a", null, true));
      assertTrue(teamARoot.ok() || teamARoot.error.contains("already"));
      assertEquals("/team-a", teamARoot.namespace.path);

      // Create project namespace
      var flooperNs = ns.createNamespace(nsCreate(steveKey.kid, "flooper-api", teamARoot.namespace.nsId, true));
      assertTrue(flooperNs.ok() || flooperNs.error.contains("already"));
      assertEquals("/team-a/flooper-api", flooperNs.namespace.path);

      // Create API keys for Linda and Jerry
      var lindaRes = ks.createApiKey(keyCmd(steveKey.kid, "linda-key", OtRole.Application));
      var lindaKey = lindaRes.key;
      assertTrue(lindaRes.ok() || lindaRes.error.contains("already"));

      var jerryRes = ks.createApiKey(keyCmd(steveKey.kid, "jerry-key", OtRole.Application));
      var jerryKey = jerryRes.key;
      assertTrue(jerryRes.ok() || jerryRes.error.contains("already"));

      // Verify that Steve can see Linda and Jerry's keys
      var kList = ks.loadKeysOf(steveKey.kid, 10, null);
      assertEquals(2, kList.page.items.size());

      // Grant Linda and Jerry access to flooper-api namespace
      var lindaAccess = ns.assignNamespaceKey(OtNamespaceOp.nsAssign(lindaKey.kid, steveKey.kid, flooperNs.namespace.nsId, true));
      assertTrue(lindaAccess.ok());
      var jerryAccess = ns.assignNamespaceKey(OtNamespaceOp.nsAssign(jerryKey.kid, steveKey.kid, flooperNs.namespace.nsId, true));
      assertTrue(jerryAccess.ok());

      // Verify Steve can see Linda and Jerry's namespace key assignments, as well as his own assignment
      var assignments = ns.loadAssignmentsBy(steveKey.kid, flooperNs.namespace.nsId, 10, null);
      assertTrue(assignments.ok());
      assertEquals(3, assignments.apiKeys.size());

      // Verify Steve has access to both namespaces
      var steveRootAccess = ns.loadKeyNamespace(steveKey.kid, teamARoot.namespace.nsId, true);
      assertNotNull(steveRootAccess);
      var steveProjectAccess = ns.loadKeyNamespace(steveKey.kid, flooperNs.namespace.nsId, true);
      assertNotNull(steveProjectAccess);

      // Verify Linda and Jerry can create namespaces in the flooper-api namespace
      var lindaSubNs = ns.createNamespace(nsCreate(lindaKey.kid, "linda-feature", flooperNs.namespace.nsId, true));
      assertEquals("/team-a/flooper-api/linda-feature", lindaSubNs.namespace.path);
      var jerrySubNs = ns.createNamespace(nsCreate(jerryKey.kid, "jerry-feature", flooperNs.namespace.nsId, true));
      assertEquals("/team-a/flooper-api/jerry-feature", jerrySubNs.namespace.path);

      // Verify Steve has access to Linda's and Jerry's namespaces
      var steveLindaAccess = ns.lookUpKeyNamespace(steveKey.kid, lindaSubNs.namespace.nsId, true);
      assertNotNull(steveLindaAccess);
      var steveJerryAccess = ns.lookUpKeyNamespace(steveKey.kid, jerrySubNs.namespace.nsId, true);
      assertNotNull(steveJerryAccess);

      // Verify Linda and Jerry cannot create namespaces under team-a root
      var bad0 = ns.createNamespace(nsCreate(lindaKey.kid, "unauthorized", teamARoot.namespace.nsId, true));
      assertFalse(bad0.ok());
      var bad1 = ns.createNamespace(nsCreate(jerryKey.kid, "unauthorized", teamARoot.namespace.nsId, true));
      assertFalse(bad1.ok());
    });

    it("Unseals a master key, reads/writes encrypted values",  () -> {
      var vs = f.instance(OtSealService.class);
      vs.unsealB64(shares);
      var testPassword = "momo123";
      var encrypted = vs.encrypt(OtValue.of(1, OtValueType.String, "test-value", testPassword, null, true, 1));
      var decrypted = vs.decrypt(encrypted);
      assertEquals(testPassword, decrypted.value);
    });

    it("Creates a value in a namespace with write access", () -> {
      var valService = f.instance(OtValueService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var nsOp = nsCreate(adminKey.get().kid, "testNsValue", null, true);
      var nsResult = nsService.createNamespace(nsOp);
      assertNotNull(nsResult.namespace);

      var nsId = nsResult.namespace.nsId;
      var val = OtValue.of(nsId, OtValueType.String, "testKey", "testValue", "Test note", false, 1);
      var valOp = OtValueOp.valCmd(adminKey.get(), val);
      var result = valService.createValue(valOp);
      assertNotNull(result.val);
      assertEquals(nsId, result.val.nsId);
      assertEquals("testKey", result.val.name);
      assertEquals("testValue", result.val.value);
      assertEquals("Test note", result.val.notes);
      assertFalse(result.val.encrypted);
      assertTrue(result.val.createdAtUtcMs > 0);
    });

    it("Fails to create a value without write access", () -> {
      var valService = f.instance(OtValueService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var jerryKey = keyService.daos.akd.loadWhereNameEq("jerry-key");
      assertFalse(jerryKey.isEmpty());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var val = OtValue.of(ns.getFirst().nsId, OtValueType.String, "testKey0", "testValue", "Test note", false, 1);
      var valOp = OtValueOp.valCmd(jerryKey.getFirst(), val);
      var result = valService.createValue(valOp);
      assertFalse(result.ok());
    });

    it("Fails to create a duplicate value in the same namespace", () -> {
      var valService = f.instance(OtValueService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var nsId = ns.getFirst().nsId;
      var val = OtValue.of(nsId, OtValueType.String, "testKey", "testValue", "Test note", false, 1);
      var valOp = OtValueOp.valCmd(adminKey.get(), val);
      var result1 = valService.createValue(valOp);
      assertFalse(result1.ok());
    });

    it("Creates an encrypted value in a namespace", () -> {
      var valService = f.instance(OtValueService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var nsId = ns.getFirst().nsId;
      var val = OtValue.of(nsId, OtValueType.String, "secureKey", "sensitiveData", "Secure note", true, 1);
      var valOp = OtValueOp.valCmd(adminKey.get(), val);
      var result = valService.createValue(valOp);
      assertNotNull(result.val);
      assertEquals(nsId, result.val.nsId);
      assertEquals("secureKey", result.val.name);
      // assertEquals("sensitiveData", result.val.value); // Decrypted in response
      assertEquals("Secure note", result.val.notes);
      assertTrue(result.val.encrypted);
      assertTrue(result.val.createdAtUtcMs > 0);

      var storedValue = valService.valuesOf(adminKey.get().kid, nsId, 10, null);
      assertTrue(storedValue.ok());
      assertEquals(2, storedValue.page.items.size());
      var storedVal = storedValue.page.items.getFirst();
      assertEquals("secureKey", storedVal.name);
      assertTrue(storedVal.encrypted);
    });

    it("Creates a configuration in a namespace with write access", () -> {
      var cs = f.instance(OtConfigService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var nsId = ns.getFirst().nsId;
      var cfg = new OtConfig();
      cfg.nsId = nsId;
      cfg.name = "testConfig";
      var op = new OtConfigOp().withApiKey(adminKey.get()).withConfig(cfg);
      var result = cs.createConfig(op);

      assertNotNull(result.cfg);
      assertEquals(nsId, result.cfg.nsId);
      assertEquals("testConfig", result.cfg.name);
      assertTrue(result.cfg.createdAtUtcMs > 0);
    });

    it("Fails to create a configuration without write access", () -> {
      var cs = f.instance(OtConfigService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var jerryKey = keyService.daos.akd.loadWhereNameEq("jerry-key");
      assertFalse(jerryKey.isEmpty());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var nsId = ns.getFirst().nsId;
      var cfg = new OtConfig();
      cfg.nsId = nsId;
      cfg.name = "unauthorizedConfig";
      var op = new OtConfigOp().withApiKey(jerryKey.getFirst()).withConfig(cfg);
      var result = cs.createConfig(op);
      assertFalse(result.ok());
    });

    it("Fails to create a duplicate configuration in the same namespace", () -> {
      var cs = f.instance(OtConfigService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var nsService = f.instance(OtNamespaceService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var ns = nsService.daos.nsd.loadWhereNameEq("testNsValue");
      var nsId = ns.getFirst().nsId;
      var cfg = new OtConfig();
      cfg.nsId = nsId;
      cfg.name = "testConfig";
      var op = new OtConfigOp().withApiKey(adminKey.get()).withConfig(cfg);
      var result = cs.createConfig(op);
      assertFalse(result.ok());
    });

    it("Lists accessible user values", () -> {
      var valService = f.instance(OtValueService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());
      var res = valService.accessibleValuesFor(adminKey.get());
      assertTrue(res.ok());
    });

    it("Adds nodes to a configuration and loads them with/without decryption", () -> {
      var cs = f.instance(OtConfigService.class);
      var valService = f.instance(OtValueService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var adminKey = keyService.loadRootKey();
      assertTrue(adminKey.isPresent());

      var testVal = valService.daos.vld.loadWhereNameEq("testKey").getFirst();
      var secureVal = valService.daos.vld.loadWhereNameEq("secureKey").getFirst();
      var cfg = cs.daos.cfd.loadWhereNameEq("testConfig").getFirst();

      // Add root node
      var rootNode = new OtNode();
      rootNode.cid = cfg.cid;
      rootNode.label = "root";
      rootNode.type = OtNodeType.Value;
      rootNode.vid = testVal.vid;
      rootNode.pNid = null;
      rootNode.itemIdx = null;

      var rootV = OtVar.of(rootNode, testVal);
      var op = new OtConfigOp().withApiKey(adminKey.get()).withVars(rootV).withConfig(cfg);
      var result = cs.update(op);
      assertNotNull(rootNode.nid);

      // Add child node with encrypted value
      var childNode = new OtNode();
      childNode.cid = cfg.cid;
      childNode.label = "secureChild";
      childNode.type = OtNodeType.Value;
      childNode.vid = secureVal.vid;
      childNode.nid = -2;
      childNode.pNid = rootNode.nid;
      childNode.itemIdx = null;

      var childV = OtVar.of(childNode, secureVal);
      op = new OtConfigOp().withApiKey(adminKey.get()).withVars(rootV, childV).withConfig(cfg);
      result = cs.update(op);
      assertTrue(result.ok());
      assertNotNull(childNode.nid);

      op.encrypted = true;
      op.error = null;
      op.validations.clear();
      var load = cs.load(op);
      assertTrue(load.ok());

      var g = f.instance(Gson.class);
      OtOptions.log.info(g.toJson(load));
    });

    it("Fails to add a node without write access", () -> {
      var cs = f.instance(OtConfigService.class);
      var keyService = f.instance(OtApiKeyService.class);
      var jerryKey = keyService.daos.akd.loadWhereNameEq("jerry-key").getFirst();
      var cfg = cs.daos.cfd.loadWhereNameEq("testConfig").getFirst();
      var op = new OtConfigOp().withApiKey(jerryKey).withVars().withConfig(cfg);
      var result = cs.update(op);
      assertFalse(result.ok());
    });

    it("Shuts down the database", () -> {
      var ds = f.instance(HikariDataSource.class);
      assertNotNull(ds);
      ds.close();
    });
  }

}
