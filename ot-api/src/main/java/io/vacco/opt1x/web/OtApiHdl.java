package io.vacco.opt1x.web;

import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.*;
import io.vacco.ronove.RvAttachmentParam;
import jakarta.ws.rs.*;
import java.util.Objects;

import static io.vacco.opt1x.schema.OtConstants.*;

public class OtApiHdl {

  private final OtSealService       sealService;
  private final OtApiKeyService     keyService;
  private final OtNamespaceService  nsService;
  private final OtValueService      valService;
  private final OtConfigService     cfgService;

  public OtApiHdl(OtSealService sealService, OtApiKeyService keyService,
                  OtNamespaceService nsService, OtValueService valService,
                  OtConfigService cfgService) {
    this.sealService = Objects.requireNonNull(sealService);
    this.keyService  = Objects.requireNonNull(keyService);
    this.nsService   = Objects.requireNonNull(nsService);
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
    cmd.parentKid = myKey.kid;
    return keyService.createApiKey(cmd);
  }

  @GET @Path(apiV1NamespaceId)
  public OtList<OtNamespace, String> apiV1NamespaceIdGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                         @PathParam(OtNamespaceDao.fld_nsId) Integer nsId) {
    return nsService.loadNamespace(myKey.kid, nsId);
  }

  @GET @Path(apiV1Namespace)
  public OtList<OtNamespace, String> apiV1NamespaceGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                       @QueryParam(qPageSize) int pageSize,
                                                       @QueryParam(qNext) String next) {
    return nsService.loadNamespacesOf(myKey.kid, pageSize, next);
  }

  @POST @Path(apiV1Namespace)
  public OtNamespaceOp apiV1NamespacePost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                          @BeanParam OtNamespaceOp cmd) {
    if (cmd.keyNamespace != null) {
      cmd.keyNamespace.kid = myKey.kid;
      cmd.keyNamespace.grantKid = myKey.kid;
    }
    return nsService.createNamespace(cmd);
  }

  @GET @Path(apiV1NamespaceKey)
  public OtAssignmentList apiV1NamespaceKeyGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                               @QueryParam(qPageSize) int pageSize,
                                               @QueryParam(OtKeyNamespaceDao.fld_nsId) Integer nsId,
                                               @QueryParam(qNext) Long next) {
    return nsService.loadAssignmentsBy(myKey.kid, nsId, pageSize, next);
  }

  @POST @Path(apiV1NamespaceKey)
  public OtNamespaceOp apiV1NamespaceKeyPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                             @BeanParam OtNamespaceOp cmd) {
    if (cmd.keyNamespace != null) {
      cmd.keyNamespace.grantKid = myKey.kid;
    }
    return nsService.assignNamespaceKey(cmd);
  }

  @GET @Path(apiV1NamespaceIdValue)
  public OtList<OtValue, String> apiV1NamespaceIdValueGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                          @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                                          @QueryParam(qPageSize) int pageSize,
                                                          @QueryParam(qNext) String next) {
    return valService.valuesOf(myKey.kid, nsId, pageSize, next);
  }

  @POST @Path(apiV1NamespaceIdValue)
  public OtValueOp apiV1NamespaceIdValuePost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                             @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                             @BeanParam OtValueOp cmd) {
    if (cmd.val != null) {
      cmd.val.nsId = nsId;
      cmd.val.createdAtUtcMs = 1;
    }
    return valService.createValue(cmd.withApiKey(myKey));
  }

  @GET @Path(apiV1Value)
  public OtValueOp apiV1ValueGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey) {
    return valService.accessibleValuesFor(myKey);
  }

  @GET @Path(apiV1NamespaceIdConfig)
  public OtList<OtConfig, String> apiV1NamespaceIdConfigGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                            @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                                            @QueryParam(qPageSize) int pageSize,
                                                            @QueryParam(qNext) String next) {
    return cfgService.configsOf(myKey.kid, nsId, pageSize, next);
  }

  @POST @Path(apiV1NamespaceIdConfig)
  public OtConfigOp apiV1NamespaceIdConfigPost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                               @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                               @BeanParam OtConfigOp cmd) {
    cmd.key = myKey;
    if (cmd.cfg != null) {
      cmd.cfg.nsId = nsId;
    }
    return cfgService.createConfig(cmd);
  }

  @POST @Path(apiV1NamespaceIdConfigIdNode)
  public OtConfigOp apiV1NamespaceIdConfigIdNodePost(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                     @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                                     @PathParam(OtConfigDao.fld_cid) Integer cid,
                                                     @BeanParam OtConfigOp cmd) {
    cmd.key = myKey;
    if (cmd.vars != null && cmd.cfg != null) {
      cmd.cfg.cid = cid;
      cmd.cfg.nsId = nsId;
      for (var otv : cmd.vars) {
        otv.node.cid = cid;
      }
    }
    return cfgService.update(cmd);
  }

  @GET @Path(apiV1NamespaceIdConfigId)
  public OtConfigOp apiV1NamespaceIdConfigIdGet(@RvAttachmentParam(OtApiKey.class) OtApiKey myKey,
                                                @PathParam(OtNamespaceDao.fld_nsId) Integer nsId,
                                                @PathParam(OtConfigDao.fld_cid) Integer cid,
                                                @QueryParam(OtValueDao.fld_encrypted) boolean encrypted) {
    var cmd = new OtConfigOp();
    cmd.key = myKey;
    cmd.cfg = new OtConfig();
    cmd.cfg.cid = cid;
    cmd.cfg.nsId = nsId;
    cmd.encrypted = encrypted;
    return cfgService.load(cmd);
  }

}
