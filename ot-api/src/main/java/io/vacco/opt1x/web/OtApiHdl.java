package io.vacco.opt1x.web;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.*;
import io.vacco.ronove.*;
import jakarta.ws.rs.*;
import java.util.Objects;

import static io.vacco.opt1x.schema.OtGroup.group;
import static io.vacco.opt1x.dto.OtAdminOp.adminOp;
import static io.vacco.opt1x.schema.OtConstants.*;

public class OtApiHdl {

  private final OtSealService   sealService;
  private final OtApiKeyService keyService;
  private final OtAdminService  admService;
  private final OtValueService  valService;
  private final OtConfigService cfgService;

  public OtApiHdl(OtSealService sealService, OtApiKeyService keyService,
                  OtAdminService admService, OtValueService valService,
                  OtConfigService cfgService) {
    this.sealService = Objects.requireNonNull(sealService);
    this.keyService  = Objects.requireNonNull(keyService);
    this.admService  = Objects.requireNonNull(admService);
    this.valService  = Objects.requireNonNull(valService);
    this.cfgService  = Objects.requireNonNull(cfgService);
  }

  @GET @Path(apiV1Init)
  public OtInitOp apiV1InitGet() {
    return sealService.initialize();
  }

  @POST @Path(apiV1Unseal)
  public OtUnsealOp apiV1UnsealPost(@BeanParam String unsealKey) {
    return sealService.loadUnsealKey(unsealKey);
  }

  @GET @Path(apiV1Key)
  public OtList<OtApiKey, String> apiV1KeyGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                              @QueryParam(qPageSize) int pageSize,
                                              @QueryParam(qNext) String next) {
    return keyService.loadKeysOf(myKey.kid, pageSize, next);
  }

  @POST @Path(apiV1Key)
  public OtApiKeyOp apiV1KeyPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                 @BeanParam OtApiKeyOp cmd) {
    if (cmd != null && cmd.key != null) {
      cmd.key.pKid = myKey.kid;
    }
    return keyService.createApiKey(cmd);
  }

  @GET @Path(apiV1Group)
  public OtKeyAccess apiV1GroupGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey) {
    return admService.accessGroupsOf(myKey.kid);
  }

  @POST @Path(apiV1Group)
  public OtAdminOp apiV1GroupPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                  @BeanParam OtAdminOp adminOp) {
    if (adminOp.keyGroupBind != null) {
      return admService.bindKeyToGroup(adminOp.withKey(myKey));
    }
    return admService.createGroup(adminOp.withKey(myKey));
  }

  @GET @Path(apiV1GroupId)
  public OtKeyAccess apiV1GroupIdGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                     @PathParam(OtGroupDao.fld_gid) Integer gid) {
    return admService.accessGroupsOf(myKey.kid, gid);
  }

  @DELETE @Path(apiV1GroupId)
  public OtAdminOp apiV1GroupIdDelete(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                      @PathParam(OtGroupDao.fld_gid) Integer gid) {
    var grp = group(null, null);
    grp.gid = gid;
    return admService.deleteGroup(adminOp().withKey(myKey).withGroup(grp));
  }

  @GET @Path(apiV1Namespace)
  public OtKeyAccess apiV1NamespaceGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey) {
    return admService.accessNamespacesOf(myKey.kid);
  }

  @POST @Path(apiV1Namespace)
  public OtAdminOp apiV1NamespacePost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                      @BeanParam OtAdminOp adminOp) {
    if (adminOp.groupNs != null) {
      return admService.bindGroupToNamespace(adminOp.withKey(myKey));
    }
    return admService.createNamespace(adminOp.withKey(myKey));
  }

  @GET @Path(apiV1NamespaceId)
  public OtKeyAccess apiV1NamespaceIdGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                         @PathParam(OtNamespaceDao.fld_nsId) Integer nsId) {
    return admService.accessNamespacesOf(myKey.kid, nsId);
  }

  @GET @Path(apiV1ValueNsId)
  public OtValueOp apiV1ValueNsIdGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                     @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                     @QueryParam(qPageSize) int pageSize,
                                     @QueryParam(qNext) String next) {
    return valService.valuesOf(myKey.kid, nsId, pageSize, next);
  }

  @POST @Path(apiV1ValueNsId)
  public OtValueOp apiV1ValueNsIdPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                      @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                      @BeanParam OtValueOp cmd) {
    if (cmd.val != null) {
      cmd.val.nsId = nsId;
    }
    return valService.createValue(cmd.withKey(myKey));
  }

  @GET @Path(apiV1Value)
  public OtValueOp apiV1ValueGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey) {
    return valService.accessibleValuesFor(myKey);
  }

  @GET @Path(apiV1Config)
  public OtList<OtConfig, String> apiV1ConfigGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                 @QueryParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                                 @QueryParam(qPageSize) int pageSize,
                                                 @QueryParam(qNext) String next) {
    return cfgService.configsOf(myKey.kid, nsId, pageSize, next);
  }

  @POST @Path(apiV1Config)
  public OtConfigOp apiV1ConfigPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                    @QueryParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                    @BeanParam OtConfigOp cmd) {
    cmd.key = myKey;
    if (cmd.cfg != null) {
      cmd.cfg.nsId = nsId;
    }
    return cfgService.createConfig(cmd);
  }

  @GET @Path(apiV1ConfigCid)
  public OtConfigOp apiV1ConfigCidGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                      @PathParam(OtConfigDao.fld_cid) Integer cid,
                                      @QueryParam(OtValueDao.fld_encrypted) boolean encrypted) {
    var cmd = new OtConfigOp();
    cmd.key = myKey;
    cmd.cfg = new OtConfig();
    cmd.cfg.cid = cid;
    cmd.encrypted = encrypted;
    return cfgService.load(cmd);
  }

  @POST @Path(apiV1ConfigCid)
  public OtConfigOp apiV1ConfigCidPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                       @PathParam(OtConfigDao.fld_cid) Integer cid,
                                       @BeanParam OtConfigOp cmd) {
    cmd.key = myKey;
    if (cmd.vars != null && cmd.cfg != null) {
      cmd.cfg.cid = cid;
      for (var otv : cmd.vars) {
        otv.node.cid = cid;
      }
    }
    return cfgService.update(cmd);
  }

  @GET @Path(apiV1ConfigIdFmt)
  public RvResponse<Object> apiV1ConfigIdFmtGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                @PathParam(OtConfigDao.fld_cid) Integer cid,
                                                @PathParam(kFmt) String otFmt,
                                                @QueryParam(OtValueDao.fld_encrypted) Boolean encrypted) {
    return cfgService.render(myKey, cid, otFmt, encrypted == null || encrypted);
  }

}
