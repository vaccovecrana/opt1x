package io.vacco.opt1x.context;

import com.google.gson.Gson;
import com.zaxxer.hikari.*;
import io.rqlite.jdbc.L4Log;
import io.vacco.metolithe.changeset.*;
import io.vacco.metolithe.core.MtLog;
import io.vacco.metolithe.query.MtJdbc;
import io.vacco.murmux.http.MxLog;
import io.vacco.opt1x.impl.*;
import org.codejargon.feather.Provides;
import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.util.Objects;

import static io.vacco.opt1x.impl.OtOptions.log;

public class OtService {

  @Provides @Singleton
  public HikariDataSource dataSource() {
    L4Log.traceFn = log::trace;
    var hkConfig = new HikariConfig();
    hkConfig.setJdbcUrl(OtOptions.jdbcUrl);
    return new HikariDataSource(hkConfig);
  }

  @Provides @Singleton
  public MtJdbc mtJdbc(HikariDataSource hkDs, Gson g) throws Exception {
    MtLog.setInfoLogger(log::info);
    MtLog.setDebugLogger(log::debug);
    MtLog.setWarnLogger(log::warn);
    MxLog.setDebugLogger(log::debug);
    MxLog.setInfoLogger(log::info);
    MxLog.setWarnLogger(log::warn);
    MxLog.setErrorLogger(log::error);
    L4Log.debugFn = log::debug;
    L4Log.traceFn = log::info;
    try (var conn = hkDs.getConnection()) {
      var logUrl = Objects.requireNonNull(OtService.class.getResource("/ot-schema.json"));
      try (var ir = new InputStreamReader(logUrl.openStream())) {
        var chgSet = g.fromJson(ir, MtChangeSet.class);
        new MtApply(conn, null)
          .withAutoCommit(true)
          .applyChanges(chgSet.changes, null);
      }
    }
    return new MtJdbc(hkDs);
  }

  @Provides @Singleton
  public OtDaos otDaos(MtJdbc db) {
    return new OtDaos(db, OtOptions.getSchema());
  }

  @Provides @Singleton
  public OtApiKeyService otKeyService(OtDaos daos) {
    return new OtApiKeyService(daos);
  }

  @Provides @Singleton
  public OtNamespaceService otNamespaceService(OtDaos daos) {
    return new OtNamespaceService(daos);
  }

  @Provides @Singleton
  public OtSealService otSealService(Gson json, OtApiKeyService keyService) {
    return new OtSealService(json, keyService, OtOptions.shares, OtOptions.threshold);
  }

  @Provides @Singleton
  public OtValueService otValueService(OtDaos daos, OtNamespaceService nss, OtSealService ss) {
    return new OtValueService(daos, nss, ss);
  }

  @Provides @Singleton
  public OtConfigService configService(OtDaos daos, OtValueService valService,
                                       OtNamespaceService nsService, OtSealService sealService) {
    return new OtConfigService(daos, valService, nsService, sealService);
  }

}
