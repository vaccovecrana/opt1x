package io.vacco.opt1x.impl;

import io.vacco.opt1x.dao.OtDaos;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;

import static io.vacco.opt1x.schema.OtConstants.Opt1x;
import static io.vacco.opt1x.schema.OtGroup.*;
import static io.vacco.opt1x.schema.OtNamespace.*;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static java.util.stream.Collectors.*;
import static java.lang.String.format;

public class OtAdminService {

  public  final OtDaos daos;
  private final OtApiKeyService keyService;

  public OtAdminService(OtDaos daos, OtApiKeyService keyService) {
    this.daos = Objects.requireNonNull(daos);
    this.keyService = Objects.requireNonNull(keyService);
  }

  private List<OtNamespace> pathOfNs(Integer nsId) {
    var out = new ArrayList<OtNamespace>();
    var id = nsId;
    while (id != null) {
      var ns = daos.nsd.loadExisting(id);
      out.add(ns);
      id = ns.pNsId;
    }
    return out;
  }

  private List<OtGroup> pathOfGroup(Integer gid) {
    var out = new ArrayList<OtGroup>();
    var id = gid;
    while (id != null) {
      var grp = daos.grd.loadExisting(id);
      out.add(grp);
      id = grp.pGid;
    }
    return out;
  }

  public <T extends OtResult> T noNsAccess(T cmd, Integer kid, Integer nsId, String type) {
    return cmd.withError(format(
      "Admin - key [%d] has no [%s] access into namespace [%d]",
      kid, type, nsId
    ));
  }

  public Optional<OtKeyGroup> memberOf(Integer kid, Integer gid, OtGroupRole role) {
    var groupPath = pathOfGroup(gid);
    for (var grp : groupPath) {
      var kgx = daos.kgd.loadWhereKidEq(kid).stream()
        .filter(kg -> kg.gid.equals(grp.gid))
        .filter(kg -> kg.role.includes(role))
        .findFirst();
      if (kgx.isPresent()) {
        return kgx;
      }
    }
    return Optional.empty();
  }

  public List<OtGroupNs> accessGroupsOf(Integer nsId, boolean r, boolean w, boolean m) {
    var nsIds = pathOfNs(nsId).stream().map(ns -> ns.nsId).toArray(Integer[]::new);
    return daos.gnd.listWhereNsIdIn(nsIds).stream()
      .filter(gns -> !r || gns.read)
      .filter(gns -> !w || gns.write)
      .filter(gns -> !m || gns.manage)
      .collect(toList());
  }

  public OtKeyAccess accessGroupsOf(Integer kid) {
    var out = new OtKeyAccess();
    out.keyGroups = daos.kgd.loadPageItems(
      daos.kgd.query()
        .from(daos.akd.dsc)
        .innerJoin(daos.kgd.dsc, daos.akd.dsc)
        .eq(daos.akd.fld_kid(), kid)
    );
    out.groups = daos.grd.loadPageItems(
      daos.grd.query()
        .from(daos.akd.dsc)
        .innerJoin(daos.kgd.dsc, daos.akd.dsc)
        .innerJoin(daos.grd.dsc, daos.kgd.dsc)
        .eq(daos.akd.fld_kid(), kid)
    );
    var tree = new TreeMap<String, OtGroup>();  // TODO find a way to improve this group expansion logic
    for (var grp : out.groups) {
      tree.putIfAbsent(grp.path, grp);
      var branch = daos.grd.loadPageItems(
        daos.grd.query()
          .like(daos.grd.fld_path(), daos.likeFmt(grp.path))
          .and()
          .neq(daos.grd.fld_path(), grp.path)
      );
      for (var grp0 : branch) {
        tree.putIfAbsent(grp0.path, grp0);
      }
    }
    out.groupTree = new ArrayList<>(tree.values());
    return out;
  }

  public OtKeyAccess accessGroupsOf(Integer kid, Integer gid) {
    var out = new OtKeyAccess();
    var g = daos.grd.loadExisting(gid);
    var ka = memberOf(kid, gid, OtGroupRole.Admin);
    out.group = g;
    if (ka.isPresent()) {
      out.groups = daos.grd.loadWherePGidEq(g.gid);
    }
    out.keys = keyService.allKeysOf(kid);
    out.keyGroups = daos.kgd.listWhereGidIn(gid);
    return out;
  }

  public OtKeyAccess accessNamespacesOf(Integer kid) {
    var out = accessGroupsOf(kid);
    out.groupNamespaces = daos.gnd.loadPageItems(
      daos.gnd.query()
        .from(daos.akd.dsc)
        .innerJoin(daos.kgd.dsc, daos.akd.dsc)
        .innerJoin(daos.grd.dsc, daos.kgd.dsc)
        .innerJoin(daos.gnd.dsc, daos.grd.dsc)
        .eq(daos.kgd.fld_kid(), kid)
    );
    out.namespaces = daos.nsd.loadPageItems(
      daos.nsd.query()
        .from(daos.akd.dsc)
        .innerJoin(daos.kgd.dsc, daos.akd.dsc)
        .innerJoin(daos.grd.dsc, daos.kgd.dsc)
        .innerJoin(daos.gnd.dsc, daos.grd.dsc)
        .innerJoin(daos.nsd.dsc, daos.gnd.dsc)
        .eq(daos.akd.fld_kid(), kid)
    );
    var tree = new TreeMap<String, OtNamespace>();
    for (var ns : out.namespaces) {
      tree.putIfAbsent(ns.path, ns);
      var branch = daos.nsd.loadPageItems(
        daos.nsd.query()
          .like(daos.nsd.fld_path(), daos.likeFmt(ns.path))
          .and()
          .neq(daos.nsd.fld_path(), ns.path)
      );
      for (var ns0 : branch) {
        tree.putIfAbsent(ns0.path, ns0);
      }
      out.namespaceTree = new ArrayList<>(tree.values());
    }
    return out;
  }

  public OtKeyAccess accessNamespacesOf(Integer kid, Integer nsId) {
    var out = accessNamespacesOf(kid);
    out.namespace = daos.nsd.loadExisting(nsId);
    out.namespaces = daos.nsd.loadWherePNsIdEq(nsId);
    var nsIdGns = daos.gnd.listWhereNsIdIn(nsId);
    for (var gns : nsIdGns) {
      if (!out.groupNamespaces.contains(gns)) {
        out.groupNamespaces.add(gns);
      }
    }
    var nsIdGroups = daos.grd.listWhereGidIn(nsIdGns.stream().map(gns -> gns.gid).toArray(Integer[]::new));
    for (var grp : nsIdGroups) {
      if (!out.groups.contains(grp)) {
        out.groups.add(grp);
      }
    }
    return out;
  }

  public Optional<OtKeyGroup> canAccessNs(Integer kid, Integer nsId, boolean r, boolean w, boolean m) {
    for (var gns : accessGroupsOf(nsId, r, w, m)) {
      var kg = memberOf(kid, gns.gid, OtGroupRole.Member);
      if (kg.isPresent()) {
        return kg;
      }
    }
    return Optional.empty();
  }

  public OtAdminOp existsNs(OtAdminOp cmd, Integer nsId) {
    var ns = daos.nsd.load(nsId);
    if (ns.isEmpty()) {
      return cmd.withError(format("Admin - namespace [%d] does not exist", nsId));
    }
    return cmd;
  }

  public OtAdminOp existsGroup(OtAdminOp cmd, Integer gid) {
    var grp = daos.grd.load(gid);
    if (grp.isEmpty()) {
      return cmd.withError(format("Admin - group [%d] does not exist", gid));
    }
    return cmd;
  }

  public OtAdminOp duplicateNs(OtAdminOp cmd) {
    var ns0 = daos.nsd.loadWherePathEq(cmd.ns.path);
    if (!ns0.isEmpty()) {
      return cmd
        .withNs(ns0.getFirst())
        .withError(format("Admin - namespace [%s] already exists", cmd.ns.path));
    }
    return cmd;
  }

  public OtAdminOp duplicateGroup(OtAdminOp cmd) {
    var grp0 = daos.grd.loadWherePathEq(cmd.group.path);
    if (!grp0.isEmpty()) {
      return cmd
        .withGroup(grp0.getFirst())
        .withError(format("Admin - group [%s] already exists", cmd.group.path));
    }
    return cmd;
  }

  public OtAdminOp canManageNs(OtAdminOp cmd) {
    var nsId = cmd.ns.nsId != null ? cmd.ns.nsId : cmd.ns.pNsId;
    var kg = canAccessNs(cmd.key.kid, nsId, false, false, true);
    if (kg.isPresent()) {
      return cmd.withKeyGroup(kg.get());
    }
    return cmd.withError(format(
      "Admin - key [%d] does not have admin group access to namespace [%d]",
      cmd.key.kid, cmd.ns.pNsId
    ));
  }

  public OtAdminOp canManageGroup(OtAdminOp cmd) {
    var gid = cmd.group.gid != null ? cmd.group.gid : cmd.group.pGid;
    var kg = memberOf(cmd.key.kid, gid, OtGroupRole.Admin);
    if (kg.isPresent()) {
      return cmd.withKeyGroup(kg.get());
    }
    return cmd.withError(format(
      "Admin - key [%d] does not have admin access to group [%d]",
      cmd.key.kid, cmd.group.pGid
    ));
  }

  public OtAdminOp newNs(OtAdminOp cmd) {
    var pns = daos.nsd.loadExisting(cmd.ns.pNsId);
    cmd.withNs(namespace(
      pns.nsId, cmd.ns.name,
      format("%s/%s", pns.path, cmd.ns.name),
      System.currentTimeMillis()
    ));
    return OtValid.validate(cmd.ns, cmd);
  }

  public OtAdminOp newGroup(OtAdminOp cmd) {
    var pg = daos.grd.loadExisting(cmd.group.pGid);
    cmd.withGroup(group(
      pg.gid, cmd.group.name,
      format("%s/%s", pg.path, cmd.group.name),
      System.currentTimeMillis()
    ));
    return OtValid.validate(cmd.group, cmd);
  }

  public OtAdminOp createNamespace(OtAdminOp cmd) {
    try {
      cmd.clear();
      cmd = cmd
        .validate(this::canManageNs)
        .validate(cmd0 -> existsNs(cmd0, cmd0.ns.pNsId));
      if (cmd.ok()) {
        cmd = cmd
          .validate(this::newNs)
          .validate(this::duplicateNs);
        if (cmd.ok()) {
          daos.nsd.save(cmd.ns);
        }
      }
      return cmd.clearKeyGroup();
    } catch (Exception e) {
      onError("Admin - Namespace create error", e);
      return cmd.clearKeyGroup().withError(e);
    }
  }

  public OtAdminOp createGroup(OtAdminOp cmd) {
    try {
      cmd.clear();
      cmd = cmd
        .validate(this::canManageGroup)
        .validate(cmd0 -> existsGroup(cmd0, cmd0.group.pGid));
      if (cmd.ok()) {
        cmd = cmd
          .validate(this::newGroup)
          // TODO add parent/child group relationship? Or is checking the parent group for management permissions enough?
          .validate(this::duplicateGroup);
        if (cmd.ok()) {
          daos.grd.save(cmd.group);
        }
      }
      return cmd.clearKeyGroup();
    } catch (Exception e) {
      onError("Admin - Group create error", e);
      return cmd.clearKeyGroup().withError(e);
    }
  }

  public OtAdminOp deleteGroup(OtAdminOp cmd) {
    try {
      cmd.clear();
      cmd = cmd
        .validate(this::canManageGroup)
        .validate(cmd0 -> existsGroup(cmd0, cmd0.group.gid));
      if (cmd.ok()) {
        var group = daos.grd.loadExisting(cmd.group.gid);
        if (group.name.equals(Opt1x)) {
          return cmd.withError(format("Admin - the %s root group cannot be deleted", Opt1x));
        }
        var groups = daos.grd.loadPageItems(
          daos.grd.query()
            .like(daos.grd.fld_path(), daos.likeFmt(group.path))
        );
        groups.sort(Comparator.comparing(grp -> grp.path));
        Collections.reverse(groups);
        daos.onTxResult(cmd, daos.grd.sql().tx((tx, conn) -> {
          for (var grp : groups) {
            daos.kgd.deleteWhereGidEq(grp.gid);
            daos.gnd.deleteWhereGidEq(grp.gid);
            daos.grd.deleteWhereGidEq(grp.gid);
          }
        }));
        cmd.withGroup(group);
      }
      return cmd;
    } catch (Exception e) {
      onError("Admin  - Group delete error", e);
      return cmd.withError(e);
    }
  }

  public OtAdminOp bindGroupToNamespace(OtAdminOp cmd) {
    try {
      cmd.clear();
      cmd = cmd
        .validate(this::canManageGroup)
        .validate(this::canManageNs)
        .validate(cmd0 -> {
          cmd0.groupNs.gid = cmd0.group.gid;
          cmd0.groupNs.nsId = cmd0.ns.nsId;
          cmd0.groupNs.grantKid = cmd0.key.kid;
          cmd0.groupNs.grantUtcMs = System.currentTimeMillis();
          return OtValid.validate(cmd0.groupNs, cmd0);
        });
      if (cmd.ok()) {
        daos.gnd.upsert(cmd.groupNs);
      }
      return cmd.clearKeyGroup();
    } catch (Exception e) {
      onError("Admin - Group/Namespace binding error", e);
      return cmd.clearKeyGroup().withError(e);
    }
  }

  public OtAdminOp bindKeyToGroup(OtAdminOp cmd) {
    try {
      cmd.clear();
      cmd = cmd
        .validate(this::canManageGroup)
        .validate(cmd0 -> {
          cmd0.keyGroupBind.gid = cmd0.group.gid;
          cmd0.keyGroupBind.grantKid = cmd0.key.kid;
          cmd0.keyGroupBind.grantUtcMs = System.currentTimeMillis();
          return OtValid.validate(cmd0.keyGroupBind, cmd0);
        });
      if (cmd.ok()) {
        var grp = daos.grd.loadExisting(cmd.group.gid);
        if (grp.name.equals(Opt1x)) {
          return cmd.withError(format("Admin - do not bind any keys to the %s group", Opt1x));
        }
        daos.kgd.upsert(cmd.keyGroupBind);
      }
      return cmd.clearKeyGroup();
    } catch (Exception e) {
      onError("Admin - Key/Group binding error", e);
      return cmd.clearKeyGroup().withError(e);
    }
  }

}
