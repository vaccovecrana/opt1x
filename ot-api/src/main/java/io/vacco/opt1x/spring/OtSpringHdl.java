package io.vacco.opt1x.spring;

import com.google.gson.Gson;
import io.vacco.murmux.http.*;
import io.vacco.opt1x.dto.OtConfigOp;
import io.vacco.opt1x.impl.*;
import io.vacco.opt1x.schema.*;
import io.vacco.ronove.RvResponse;
import jakarta.ws.rs.core.Response;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

import static io.vacco.opt1x.dto.OtConfigOp.configOp;
import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.schema.OtConstants.*;

public class OtSpringHdl implements MxHandler {

  public static final String Basic = "Basic ";

  private final OtSealService   sealService;
  private final OtApiKeyService keyService;
  private final OtConfigService cfgService;
  private final Gson            json;

  public OtSpringHdl(OtSealService sealService, OtApiKeyService keyService,
                     OtConfigService cfgService, Gson json) {
    this.sealService = Objects.requireNonNull(sealService);
    this.keyService  = Objects.requireNonNull(keyService);
    this.cfgService  = Objects.requireNonNull(cfgService);
    this.json        = Objects.requireNonNull(json);
  }

  private Optional<OtApiKey> auth(MxExchange xc) {
    var tokenL = xc.io.getRequestHeaders().get(kAuth);
    if (tokenL != null) {
      var token = tokenL.getFirst();
      if (token.startsWith(Basic)) {
        token = token.substring(Basic.length());
        token = new String(Base64.getDecoder().decode(token), StandardCharsets.UTF_8);
        var parts = token.split(":", 2);
        if (parts.length == 2 && parts[0].equals(Opt1x)) {
          return keyService.loadKey(parts[1]);
        }
      }
    }
    return Optional.empty();
  }

  private List<OtConfigOp> init(OtApiKey key, OtSpringApp app) {
    var nsName = app.name;
    var daos = cfgService.daos;
    var q = daos.cfd.query()
      .innerJoin(daos.nsd.dsc, daos.cfd.dsc)
      .eq(daos.nsd.fld_name(), nsName)
      .and();
    for (int i = 0; i < app.profiles.size(); i++) {
      var profile = app.profiles.get(i);
      q = q.eq(daos.cfd.fld_name(), profile);
      if (i < app.profiles.size() - 1) {
        q = q.or();
      }
    }
    var cfl = daos.cfd.loadPageItems(q);
    if (cfl.isEmpty()) {
      return Collections.emptyList();
    }
    var opIdx = cfl.stream()
      .map(cfg -> cfgService.load(
        configOp().withApiKey(key).withConfig(cfg).withEncrypted(false)
      )).collect(Collectors.toMap(op -> op.cfg.name, Function.identity()));
    var opl = new ArrayList<OtConfigOp>();
    for (var profile : app.profiles) {
      opl.add(opIdx.get(profile));
    }
    return opl;
  }

  @Override public void handle(MxExchange xc) {
    try {
      if (sealService.isEmpty() || sealService.isSealed()) {
        xc.withStatus(MxStatus._503).commit();
        return;
      }
      var oKey = auth(xc);
      if (oKey.isEmpty()) {
        xc.withHeader(kAuthenticate, "Basic realm=\"Spring Cloud Config Server\"")
          .withStatus(MxStatus._401)
          .commit();
        return;
      }
      var app = OtSpringApp.parse(xc.getPath());
      if (app.name == null || app.profiles == null || app.profiles.isEmpty()) {
        xc.withStatus(MxStatus._400).commit();
        return;
      }
      var opl = init(oKey.get(), app);
      if (opl.isEmpty()) {
        xc.withStatus(MxStatus._400).commit();
        return;
      }
      RvResponse<Object> res;
      if (app.format == OtNodeFormat.json) {
        var ns = cfgService.daos.nsd.loadWhereNameEq(app.name).getFirst();
        new OtSpringEnvelope(app, ns, opl).populate();
        res = new RvResponse<>();
        res.mediaType = MxMime.json.type;
        res.withStatus(Response.Status.OK).withBody(json.toJson(app));
      } else {
        var merged = OtConfigOpMerge.merge(opl);
        res = cfgService.render(merged, app.format);
      }
      if (res.status.getStatusCode() != MxStatus._200.code) {
        xc.withStatus(MxStatus.valueOf(res.status.getStatusCode())).commit();
        return;
      }
      xc.withStatus(MxStatus._200)
        .withBody(MxMime.of(res.mediaType), res.body.toString())
        .commit();
    } catch (Exception e) {
      onError("Spring cloud config error", e);
      xc.withStatus(MxStatus._500).commit();
    }
  }

}
