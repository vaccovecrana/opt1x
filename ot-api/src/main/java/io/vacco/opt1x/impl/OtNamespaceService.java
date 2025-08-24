package io.vacco.opt1x.impl;

import io.vacco.metolithe.util.MtPage1;
import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;

import static io.vacco.opt1x.impl.OtDataIO.isActiveKey;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.lang.String.format;
import static java.util.stream.Collectors.toSet;

public class OtNamespaceService {

  public final OtDaos daos;

  public OtNamespaceService(OtDaos daos) {
    this.daos = daos;
  }

  public Optional<OtKeyNamespace> loadKeyNamespace(Integer kid, Integer nsId, boolean writeAccess) {
    var nsStream = daos.knd.loadWhereNsIdEq(nsId)
      .stream()
      .filter(kn -> kn.kid.equals(kid));
    if (writeAccess) {
      nsStream = nsStream.filter(kn -> kn.writeAccess);
    }
    return nsStream.findFirst();
  }

  public OtList<OtNamespace, String> loadNamespace(Integer kid, Integer nsId) {
    var out = new OtList<OtNamespace, String>();
    try {
      if (loadKeyNamespace(kid, nsId, false).isEmpty()) {
        return out.withPage(new MtPage1<>());
      }
      var ons = daos.nsd.load(nsId);
      return ons.isEmpty()
        ? out.withPage(new MtPage1<>())
        : out.withPage(MtPage1.ofSingle(ons.get()));
    } catch (Exception e) {
      onError("Namespace load error", e);
      return out.withError(e);
    }
  }

  public Optional<OtKeyNamespace> lookUpKeyNamespace(Integer kid, Integer nsId, boolean writeAccess) {
    var directAccess = loadKeyNamespace(kid, nsId, writeAccess);
    if (directAccess.isPresent()) {
      return directAccess;
    }
    var visited = new HashSet<Integer>();
    var currentNs = daos.nsd.loadExisting(nsId);
    while (currentNs.pNsId != null && !visited.contains(currentNs.pNsId)) {
      visited.add(currentNs.pNsId);
      var parentAccess = loadKeyNamespace(kid, currentNs.pNsId, writeAccess);
      if (parentAccess.isPresent()) {
        return parentAccess;
      }
      currentNs = daos.nsd.loadExisting(currentNs.pNsId);
    }
    return Optional.empty();
  }

  public <T extends OtResult> T nsAccess(T cmd, Integer kid, Integer nsId, boolean writeAccess) {
    var access = loadKeyNamespace(kid, nsId, writeAccess);
    if (access.isEmpty()) {
      return cmd.withError(format(
        "Key [%d] does not have [%s] access to namespace [%d]",
        kid, writeAccess ? "write" : "read", nsId
      ));
    }
    return cmd;
  }

  public OtNamespaceOp validateAssignedKeyNamespace(OtNamespaceOp cmd) {
    if (cmd.namespace.nsId == null && cmd.namespace.pNsId == null) {
      return cmd;
    }
    var ns = lookUpKeyNamespace(
      cmd.keyNamespace.grantKid,
      cmd.namespace.nsId != null ? cmd.namespace.nsId : cmd.namespace.pNsId,
      cmd.keyNamespace.writeAccess
    );
    if (ns.isPresent()) {
      return cmd;
    }
    return cmd.withError(format(
      "API key %s does not have access to namespace %d",
      cmd.keyNamespace.grantKid, cmd.namespace.nsId
    ));
  }

  public OtNamespaceOp duplicateNamespace(OtNamespaceOp cmd) {
    var ons0 = daos.nsd.loadWherePathEq(cmd.namespace.path).stream().findFirst();
    ons0.ifPresent(ns -> {
      cmd.withError(format("Namespace [%s] already exists", ns.name));
      cmd.namespace = ns;
      cmd.keyNamespace.nsId = ns.nsId;
    });
    return cmd;
  }

  public OtNamespaceOp newNamespace(OtNamespaceOp cmd) {
    Optional<OtNamespace> oPns = cmd.namespace.pNsId != null
      ? daos.nsd.load(cmd.namespace.pNsId)
      : Optional.empty();
    if (cmd.namespace.pNsId != null && oPns.isEmpty()) {
      return cmd.withError(format(
        "Namespace [%s] cannot be assigned to missing parent namespace id [%d]",
        cmd.namespace.name, cmd.namespace.pNsId
      ));
    }
    cmd.namespace.createdAt(System.currentTimeMillis());
    cmd.namespace.path = oPns
      .map(pns -> format("%s/%s", pns.path, cmd.namespace.name))
      .orElse(format("/%s", cmd.namespace.name));
    return OtValid.validate(cmd.namespace, cmd);
  }

  private OtNamespaceOp validKey(OtNamespaceOp cmd, Integer kid) { // TODO move this to api key service
    if (kid == null) {
      return cmd.withError("API key missing");
    }
    var oApiKey = daos.akd.load(kid);
    if (oApiKey.isEmpty()) {
      return cmd.withError(format("API key [%d] not found", kid));
    }
    if (!isActiveKey(oApiKey.get())) {
      return cmd.withError(format("API key [%d] is not active", kid));
    }
    return cmd;
  }

  public OtNamespaceOp validateApiKeys(OtNamespaceOp cmd) {
    if (!validKey(cmd, cmd.keyNamespace.grantKid).ok()) {
      return cmd;
    }
    if (!validKey(cmd, cmd.keyNamespace.kid).ok()) {
      return cmd;
    }
    var apiKey = daos.akd.loadExisting(cmd.keyNamespace.grantKid);
    if (cmd.namespace.pNsId == null && apiKey.role != OtRole.Admin) { // only admin keys can create new root namespaces
      return cmd.withError(format("API key [%s] cannot create root namespaces", apiKey.name));
    }
    // TODO also validate: Auditor keys cannot create any namespaces.
    return cmd;
  }

  public OtNamespaceOp createNamespace(OtNamespaceOp cmd) {
    try {
      cmd.namespace.nsId = null;
      cmd.validations.clear();
      cmd
        .validate(this::validateApiKeys)
        .validate(this::newNamespace)
        .validate(this::duplicateNamespace)
        .validate(this::validateAssignedKeyNamespace);
      if (cmd.ok()) {
        daos.nsd.sql().tx((tx, conn) -> {
          var ns = daos.nsd.save(cmd.namespace);
          var kn = cmd.keyNamespace;
          kn.nsId = ns.rec.nsId;
          kn.grantUtcMs = System.currentTimeMillis();
          daos.knd.save(kn);
        });
      }
      return cmd;
    } catch (Exception e) {
      onError("Namespace create error", e);
      return cmd.withError(e);
    }
  }

  public OtNamespaceOp assignNamespaceKey(OtNamespaceOp cmd) {
    try {
      cmd
        .validate(this::validateApiKeys)
        .validate(this::validateAssignedKeyNamespace);
      if (cmd.ok()) {
        cmd.keyNamespace.nsId = cmd.namespace.nsId;
        cmd.keyNamespace.grantUtcMs = System.currentTimeMillis();
        var kns = daos.knd.upsert(cmd.keyNamespace);
        cmd.withKeyNamespace(kns.rec);
      }
      return cmd;
    } catch (Exception e) {
      onError("Namespace assignment error", e);
      return cmd.withError(e);
    }
  }

  public OtList<OtNamespace, String> loadNamespacesOf(Integer kid, int pageSize, String next) {
    var out = new OtList<OtNamespace, String>();
    try {
      return out.withPage(
        daos.nsd.loadPage1(pageSize, false,
          daos.nsd.query()
            .leftJoin(daos.knd.dsc, daos.nsd.dsc)
            .eq(daos.knd.fld_kid(), kid),
          OtNamespaceDao.fld_name, next
        )
      );
    } catch (Exception e) {
      onError("Namespace list error", e);
      return out.withError(e);
    }
  }

  public OtAssignmentList loadAssignmentsBy(Integer kid, Integer nsId, int pageSize, Long next) {
    var out = new OtAssignmentList();
    try {
      // TODO metolithe needs to support mapping columns from a result set into one or more mapped objects...
      var page = daos.knd.loadPage1(
        pageSize, false,
        daos.nsd.query()
          .eq(daos.knd.fld_nsId(), nsId)
          .and()
          .eq(daos.knd.fld_grantKid(), kid),
        OtKeyNamespaceDao.fld_grantUtcMs, next
      );
      var apiKeyIdx = daos.akd.loadWhereKidIn(
        page.items.stream()
          .map(kns -> kns.kid)
          .collect(toSet())
          .toArray(Integer[]::new)
      );
      var apiKeys = new ArrayList<OtApiKey>();
      for (var e : apiKeyIdx.entrySet()) {
        apiKeys.add(e.getValue().getFirst());
      }
      out.page = page;
      out.apiKeys = apiKeys;
      return out;
    } catch (Exception e) {
      onError("Namespace assignment list error", e);
      return out.withError(e);
    }
  }

}
