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

import static io.vacco.opt1x.dto.OtConfigOp.configOp;
import static io.vacco.opt1x.schema.OtConstants.Opt1x;
import static io.vacco.opt1x.schema.OtValue.*;
import static io.vacco.opt1x.dto.OtValueOp.*;
import static io.vacco.opt1x.dto.OtAdminOp.*;
import static io.vacco.opt1x.schema.OtGroup.*;
import static io.vacco.opt1x.schema.OtGroupNs.*;
import static io.vacco.opt1x.schema.OtKeyGroup.*;
import static io.vacco.opt1x.schema.OtNamespace.*;
import static io.vacco.opt1x.dto.OtApiKeyOp.keyOp;
import static io.vacco.opt1x.schema.OtApiKey.rawKey;
import static j8spec.J8Spec.*;
import static java.lang.String.format;
import static org.junit.Assert.*;

@DefinedOrder
@RunWith(J8SpecRunner.class)
public class OtServiceTest {
  static {
    OtOptions.setFrom(new String[] {
      format("%s=%s", OtOptions.kJdbcUrl,  "jdbc:h2:file:./build/opt1x;DB_CLOSE_DELAY=-1;LOCK_MODE=3;AUTO_RECONNECT=TRUE"),
      format("%s=%s", OtOptions.kLogLevel, "trace")
    });
  }

  private static final Feather f = Feather.with(new OtRoot(), new OtService());
  private static String[] shares;

  private static void okOrExisting(OtResult op) {
    assertTrue(op.ok() || op.error.contains("already"));
  }

  static {
    describe("API key management", () -> {
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
        var rk = ks.loadRootKey();
        assertTrue(rk.isPresent());
        var adminKeyRes = ks.createApiKey(keyOp().withKey(rawKey(
          rk.get().kid, "admin-key-test", false
        )));
        assertNotNull(adminKeyRes.key.kid);
        assertFalse(adminKeyRes.key.leaf);

        var appKeyName = "app-key-test";
        var result = ks.createApiKey(keyOp().withKey(
          rawKey(adminKeyRes.key.kid, appKeyName, true)
        ));
        assertNotNull(result.key);
        assertTrue(result.key.leaf);

        var appKey = result.key;
        assertNotNull(appKey.kid);
        assertEquals(adminKeyRes.key.kid, appKey.pKid);

        result = ks.createApiKey(keyOp().withKey(rawKey(
          appKey.kid, "app-key-error", true
        )));
        assertNotNull(result.error);

        var res0 = ks.loadKeysOf(adminKeyRes.key.kid, 10, null);
        assertEquals(1, res0.page.size);
        assertEquals(appKeyName, res0.page.items.getFirst().name);

        var res1 = ks.loadKeysOf(appKey.kid, 10, null);
        assertFalse(res1.page.hasNext());
      });
    });

    var flp           = "flooper";
    var flpApi        = "flooper-api";
    var flpApiDev     = "flooper-api-dev";
    var flpApiStage   = "flooper-api-stage";
    var flpApiProd    = "flooper-api-prod";
    var flpOpsSupport = "flooper-ops-support";
    var flpDevelopers = "flooper-developers";
    var steveAdminKey = "steve-admin-key";
    var lindaKey      = "linda-key";
    var jerryKey      = "jerry-key";

    describe("Group/Namespace management", () -> {
      it("Checks root key membership", () -> {
        var ks = f.instance(OtApiKeyService.class);
        var as = f.instance(OtAdminService.class);
        var rk = ks.loadRootKey();
        assertTrue(rk.isPresent());
        var rns = as.daos.nsd.loadWhereNameEq(Opt1x).getFirst();
        var rgp = as.daos.grd.loadWhereNameEq(Opt1x).getFirst();
        var access = as.accessNamespacesOf(rk.get().kid);
        assertEquals(1, access.groupNamespaces.size());
        var rgn = access.groupNamespaces.getFirst();
        assertEquals(rgp.gid, rgn.gid);
        assertEquals(rns.nsId, rgn.nsId);
        assertTrue(rgn.read);
        assertTrue(rgn.write);
        assertTrue(rgn.manage);
      });

      it("Creates a team root key, group and namespace", () -> {
        var ks = f.instance(OtApiKeyService.class);
        var as = f.instance(OtAdminService.class);
        var rk = ks.loadRootKey();
        var rns = as.daos.nsd.loadWhereNameEq(Opt1x).getFirst();
        var rgp = as.daos.grd.loadWhereNameEq(Opt1x).getFirst();
        assertTrue(rk.isPresent());

        // Steve's admin key for team flooper
        var steveRes = ks.createApiKey(keyOp().withKey(rawKey(rk.get().kid, steveAdminKey, false)));
        okOrExisting(steveRes);

        // Create flooper root namespace
        var flpNsRes = as.createNamespace(adminOp().withKey(rk.get()).withNs(namespace(rns.nsId, flp)));
        okOrExisting(flpNsRes);

        // Create flooper root group
        var flpGrpRes = as.createGroup(adminOp().withKey(rk.get()).withGroup(group(rgp.gid, flp)));
        okOrExisting(flpGrpRes);

        // Grant flooper root group admin access into flooper root namespace (using root key)
        var groupNsRes = as.bindGroupToNamespace(
          adminOp()
            .withKey(rk.get())
            .withGroup(flpGrpRes.group)
            .withNs(flpNsRes.ns)
            .withGroupNs(groupNs(null, null, null, true, true, true))
        );
        okOrExisting(groupNsRes);

        // Bind Steve's key to flooper root group, so he can manage it
        var keyGroupRes = as.bindKeyToGroup(
          adminOp()
            .withKey(rk.get())
            .withGroup(flpGrpRes.group)
            .withKeyGroupBind(keyGroup(steveRes.key.kid, null, OtGroupRole.Admin, null))
        );
        okOrExisting(keyGroupRes);
      });

      it("Allows API keys to manage delegated groups/namespaces", () -> {
        var ks = f.instance(OtApiKeyService.class);
        var as = f.instance(OtAdminService.class);
        var sk = as.daos.akd.loadWhereNameEq(steveAdminKey).getFirst();
        var flpRootNs = as.daos.nsd.loadWhereNameEq(flp).getFirst();
        var flpRootGrp = as.daos.grd.loadWhereNameEq(flp).getFirst();

        // Create flooper-api namespace
        var flpApiNsRes = as.createNamespace(adminOp().withKey(sk).withNs(namespace(flpRootNs.nsId, flpApi)));
        okOrExisting(flpApiNsRes);
        var flpApiNs = flpApiNsRes.ns;

        // Create flooper-api-dev/stage/prod namespaces
        var flpApiDevNsRes = as.createNamespace(adminOp().withKey(sk).withNs(namespace(flpApiNs.nsId, flpApiDev)));
        okOrExisting(flpApiDevNsRes);
        var flpApiStageNsRes = as.createNamespace(adminOp().withKey(sk).withNs(namespace(flpApiNs.nsId, flpApiStage)));
        okOrExisting(flpApiStageNsRes);
        var flpApiProdNsRes = as.createNamespace(adminOp().withKey(sk).withNs(namespace(flpApiNs.nsId, flpApiProd)));
        okOrExisting(flpApiProdNsRes);

        // Create flooper-prod-support group
        var flpOpsGrpRes = as.createGroup(adminOp().withKey(sk).withGroup(group(flpRootGrp.gid, flpOpsSupport)));
        okOrExisting(flpOpsGrpRes);
        // Create flooper-developers group
        var flpDevsGrpRes = as.createGroup(adminOp().withKey(sk).withGroup(group(flpRootGrp.gid, flpDevelopers)));
        okOrExisting(flpDevsGrpRes);

        // Bind flooper-ops-support group with read/write access into all flooper-api-* namespaces
        for (var ns : new OtNamespace[] { flpApiDevNsRes.ns, flpApiStageNsRes.ns, flpApiProdNsRes.ns }) {
          var groupNsRes = as.bindGroupToNamespace(
            adminOp()
              .withKey(sk)
              .withGroup(flpOpsGrpRes.group)
              .withNs(ns)
              .withGroupNs(groupNs(null, null, null, true, true, false))
          );
          okOrExisting(groupNsRes);
        }

        // Bind flooper-developers group with read/write access into flooper-api-dev/stage namespaces
        for (var ns : new OtNamespace[] { flpApiDevNsRes.ns, flpApiStageNsRes.ns }) {
          var groupNsRes = as.bindGroupToNamespace(
            adminOp()
              .withKey(sk)
              .withGroup(flpDevsGrpRes.group)
              .withNs(ns)
              .withGroupNs(groupNs(null, null, null, true, true, false))
          );
          okOrExisting(groupNsRes);
        }

        // Create API keys for Linda and Jerry
        var lindaRes = ks.createApiKey(keyOp().withKey(rawKey(sk.kid, lindaKey, false)));
        okOrExisting(lindaRes);
        var jerryRes = ks.createApiKey(keyOp().withKey(rawKey(sk.kid, jerryKey, true)));
        okOrExisting(jerryRes);

        // Rotate Jerry's key
        var jerryRotRes = ks.rotate(keyOp().withKey(jerryRes.key));
        okOrExisting(jerryRotRes);

        // Verify that Steve can see Linda and Jerry's keys
        var kList = ks.loadKeysOf(sk.kid, 10, null);
        assertEquals(2, kList.page.items.size());

        // Bind Linda's key to the flooper-ops-support group
        var lindaOpsRes = as.bindKeyToGroup(
          adminOp()
            .withKey(sk)
            .withGroup(flpOpsGrpRes.group)
            .withKeyGroupBind(keyGroup(lindaRes.key.kid, null, OtGroupRole.Member, sk.kid))
        );
        okOrExisting(lindaOpsRes);
        assertTrue(as.memberOf(lindaRes.key.kid, flpOpsGrpRes.group.gid, OtGroupRole.Member).isPresent());
        var lindaNsAccess = as.accessNamespacesOf(lindaRes.key.kid);
        assertEquals(3, lindaNsAccess.namespaces.size());

        // Bind Jerry's key to the flooper-developers group
        var jerryDevsRes = as.bindKeyToGroup(
          adminOp()
            .withKey(sk)
            .withGroup(flpDevsGrpRes.group)
            .withKeyGroupBind(keyGroup(jerryRes.key.kid, null, OtGroupRole.Member, sk.kid))
        );
        okOrExisting(jerryDevsRes);
        assertTrue(as.memberOf(jerryRes.key.kid, flpDevsGrpRes.group.gid, OtGroupRole.Member).isPresent());
        var jerryNsAccess = as.accessNamespacesOf(jerryRes.key.kid);
        assertEquals(2, jerryNsAccess.namespaces.size());

        var jerryStageAccess = as.accessNamespacesOf(jerryRes.key.kid, flpApiStageNsRes.ns.nsId);
        assertEquals(1, jerryStageAccess.keyGroups.size());
        var jerryDevsAccess = as.accessGroupsOf(jerryRes.key.kid, flpDevsGrpRes.group.gid);
        assertEquals(1, jerryDevsAccess.keyGroups.size());
        assertEquals(OtGroupRole.Member, jerryDevsAccess.keyGroups.getFirst().role);

        // Verify Linda cannot create namespaces under the flooper-api-stage namespace
        var flpApiStageBadNsRes = as.createNamespace(
          adminOp()
            .withKey(lindaRes.key)
            .withNs(namespace(flpApiStageNsRes.ns.nsId, flpApiStage + "-unauthorized"))
        );
        assertFalse(flpApiStageBadNsRes.ok());

        // Delete flooper-developers group
        var delRes = as.deleteGroup(adminOp().withKey(sk).withGroup(flpDevsGrpRes.group));
        okOrExisting(delRes);
      });
    });

    var ss = f.instance(OtSealService.class);
    var vs = f.instance(OtValueService.class);
    var as = f.instance(OtAdminService.class);
    var cs = f.instance(OtConfigService.class);

    var testConfig = "test-config";
    var testKey = "testKey";
    var secureKey = "secureKey";

    describe("Value read/write operations", () -> {
      var testVal = value(
        null, "testKey", "testNsValue",
        OtValueType.String, "Test note", false
      );

      it("Unseals a master key, reads/writes encrypted values",  () -> {
        ss.unsealB64(shares);
        var testPassword = "momo123";
        var encrypted = ss.encrypt(value(1, "test-value", testPassword, OtValueType.String, null, true));
        var decrypted = ss.decrypt(encrypted);
        assertEquals(testPassword, decrypted.val);
      });

      it("Creates a value in a namespace with write access", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();
        testVal.nsId = flpStageNs.nsId;
        var valRes = vs.upsertValue(valueOp().withKey(lk).withVal(testVal));
        okOrExisting(valRes);
        assertEquals(flpStageNs.nsId, valRes.val.nsId);
        assertEquals(testKey, valRes.val.name);
        assertEquals("testNsValue", valRes.val.val);
        assertEquals("Test note", valRes.val.notes);
        assertFalse(valRes.val.encrypted);
        assertTrue(valRes.val.createUtcMs > 0);

        var loadRes = vs.accessibleValuesFor(lk);
        okOrExisting(loadRes);
        assertFalse(loadRes.values.isEmpty());
      });

      it("Creates new value versions, replaces, and deletes them", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();

        testVal.nsId = flpStageNs.nsId;
        testVal.val = "testNsNewValue";
        testVal.notes = "Test note updated";

        var verRes0 = vs.upsertValue(valueOp().withKey(lk).withVal(testVal));
        okOrExisting(verRes0);
        assertFalse(verRes0.valVersions.isEmpty());

        testVal.val = "testNsNewNewNewValue";
        testVal.notes = "Test note second update";

        var verRes1 = vs.upsertValue(valueOp().withKey(lk).withVal(testVal));
        okOrExisting(verRes1);

        var repRes = vs.restoreValueVersion(lk, verRes1.valVersions.getFirst().vvId);
        okOrExisting(repRes);
        for (var vv : repRes.valVersions) {
          okOrExisting(vs.deleteValueVersion(lk, vv.vvId));
        }
      });

      it("Fails to create a value without write access", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpRootNs = as.daos.nsd.loadWhereNameEq(flp).getFirst();
        testVal.nsId = flpRootNs.nsId;
        var valRes = vs.upsertValue(valueOp().withKey(lk).withVal(testVal));
        assertFalse(valRes.ok());
      });

      it("Creates an encrypted value in a namespace", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();
        testVal.nsId = flpStageNs.nsId;
        testVal.name = secureKey;
        testVal.val = "sensitiveData";
        testVal.encrypted = true;
        var valRes = vs.upsertValue(valueOp().withKey(lk).withVal(testVal));
        okOrExisting(valRes);
        if (valRes.ok()) {
          assertNotEquals("sensitiveData", testVal.val);
        }
      });

      it("Creates and deletes a value", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();
        var val = value(flpStageNs.nsId, "deleteKey", "deleteValue", OtValueType.String, null, false);
        var valRes = vs.upsertValue(valueOp().withKey(lk).withVal(val));
        okOrExisting(valRes);
        valRes = vs.deleteValue(lk, valRes.val.vid);
        okOrExisting(valRes);
        assertEquals(1, (int) valRes.val.vid);
      });
    });

    describe("Configuration management", () -> {
      it("Creates a configuration in a namespace with write access", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();
        var cfg = new OtConfig();
        cfg.nsId = flpStageNs.nsId;
        cfg.name = testConfig;
        var op = new OtConfigOp().withApiKey(lk).withConfig(cfg);
        var result = cs.createConfig(op);
        okOrExisting(result);
        assertNotNull(result.cfg);
        assertEquals(flpStageNs.nsId, result.cfg.nsId);
        assertEquals(testConfig, result.cfg.name);
      });

      it("Fails to create a configuration without write access", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpRootNs = as.daos.nsd.loadWhereNameEq(flp).getFirst();
        var cfg = new OtConfig();
        cfg.nsId = flpRootNs.nsId;
        cfg.name = "unauthorized-config";
        var op = new OtConfigOp().withApiKey(lk).withConfig(cfg);
        var result = cs.createConfig(op);
        assertFalse(result.ok());
      });

      it("Fails to create a duplicate configuration in the same namespace", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpStageNs = as.daos.nsd.loadWhereNameEq(flpApiStage).getFirst();
        var cfg = new OtConfig();
        cfg.nsId = flpStageNs.nsId;
        cfg.name = testConfig;
        var op = new OtConfigOp().withApiKey(lk).withConfig(cfg);
        var result = cs.createConfig(op);
        assertFalse(result.ok());
      });

      it("Adds nodes to a configuration and loads them with/without decryption", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var testVal = vs.daos.vld.loadWhereNameEq(testKey).getFirst();
        var secureVal = vs.daos.vld.loadWhereNameEq(secureKey).getFirst();
        var cfg = cs.daos.cfd.loadWhereNameEq(testConfig).getFirst();

        // Add root node
        var rootNode = new OtNode();
        rootNode.cid = cfg.cid;
        rootNode.label = "root";
        rootNode.type = OtNodeType.Object;
        rootNode.itemIdx = 0;

        var rootV = OtVar.of(rootNode, testVal);
        var op = new OtConfigOp().withApiKey(lk).withVars(rootV).withConfig(cfg);
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
        childNode.itemIdx = 1;

        // Add child node with plain value
        var cn0 = new OtNode();
        cn0.cid = cfg.cid;
        cn0.label = "valChild";
        cn0.type = OtNodeType.Value;
        cn0.vid = testVal.vid;
        cn0.nid = -3;
        cn0.pNid = rootNode.nid;
        cn0.itemIdx = 2;

        var childV = OtVar.of(childNode, secureVal);
        var cn0V = OtVar.of(cn0, testVal);

        op = new OtConfigOp().withApiKey(lk).withVars(rootV, childV, cn0V).withConfig(cfg);
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
        var jk = as.daos.akd.loadWhereNameEq(jerryKey).getFirst();
        var cfg = cs.daos.cfd.loadWhereNameEq(testConfig).getFirst();
        var op = new OtConfigOp().withApiKey(jk).withVars().withConfig(cfg);
        var result = cs.update(op);
        assertFalse(result.ok());
      });

      it("Clones and renders a configuration", () -> {
        var lk = as.daos.akd.loadWhereNameEq(lindaKey).getFirst();
        var flpDevNs = as.daos.nsd.loadWhereNameEq(flpApiDev).getFirst();
        var cfg = cs.daos.cfd.loadWhereNameEq(testConfig).getFirst();
        cfg.name = format("%s-copy", cfg.name);
        cfg.nsId = flpDevNs.nsId;
        var cloneRes = cs.clone(configOp().withApiKey(lk).withConfig(cfg));
        okOrExisting(cloneRes);
        if (cloneRes.ok()) {
          for (var fmt : OtNodeFormat.values()) {
            var renderRes = cs.render(lk, cloneRes.cfg.cid, fmt.toString(), false);
            OtOptions.log.info(renderRes.body.toString());
          }
        }
      });
    });

    it("Shuts down the database", () -> {
      var ds = f.instance(HikariDataSource.class);
      assertNotNull(ds);
      ds.close();
    });
  }

}
