package io.vacco.opt1x.context;

import com.google.gson.Gson;
import com.zaxxer.hikari.*;
import io.rqlite.jdbc.L4Log;
import io.vacco.metolithe.changeset.*;
import io.vacco.metolithe.core.MtLog;
import io.vacco.metolithe.query.MtJdbc;
import io.vacco.murmux.http.MxLog;
import io.vacco.opt1x.dao.OtDaos;
import io.vacco.opt1x.impl.*;
import org.codejargon.feather.Provides;
import javax.inject.Singleton;
import java.io.InputStreamReader;
import java.util.Objects;

import static io.vacco.opt1x.impl.OtOptions.log;

public class OtService {

  @Provides @Singleton
  public HikariDataSource dataSource() {
    MtLog.setInfoLogger(log::info);
    MtLog.setDebugLogger(log::debug);
    MtLog.setWarnLogger(log::warn);

    MxLog.setInfoLogger(log::info);
    MxLog.setDebugLogger(log::debug);
    MxLog.setWarnLogger(log::warn);
    MxLog.setErrorLogger(log::error);

    L4Log.setInfoLogger(log::info);
    L4Log.setDebugLogger(log::debug);
    L4Log.setWarnLogger(log::warn);
    L4Log.setTraceLogger(log::trace);

    var hkConfig = new HikariConfig();
    hkConfig.setJdbcUrl(OtOptions.jdbcUrl);
    return new HikariDataSource(hkConfig);
  }

  @Provides @Singleton
  public MtJdbc mtJdbc(HikariDataSource hkDs, Gson g) throws Exception {
    if (OtOptions.jdbcUrl.contains("h2")) {
      try (var conn = hkDs.getConnection()) {
        conn.createStatement().execute("create schema if not exists main");
        conn.createStatement().execute("set schema main");
      }
    }
    try (var conn = hkDs.getConnection()) {
      var logUrl = Objects.requireNonNull(OtService.class.getResource("/ot-schema.json"));
      try (var ir = new InputStreamReader(logUrl.openStream())) {
        var chgSet = g.fromJson(ir, MtChangeSet.class);
        new MtApply(conn, OtOptions.getSchema())
          .withTransactions(!OtOptions.isRqLite())
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
  public OtApiKeyService keyService(OtDaos daos) {
    return new OtApiKeyService(daos);
  }

  @Provides @Singleton
  public OtAdminService adminService(OtDaos daos, OtApiKeyService keyService) {
    return new OtAdminService(daos, keyService);
  }

  @Provides @Singleton
  public OtSealService otSealService(Gson json, OtApiKeyService keyService) {
    return new OtSealService(json, keyService, OtOptions.shares, OtOptions.threshold);
  }

  @Provides @Singleton
  public OtValueService otValueService(OtDaos daos, OtAdminService as, OtSealService ss) {
    return new OtValueService(daos, as, ss);
  }

  @Provides @Singleton
  public OtConfigService configService(OtDaos daos, OtValueService valService,
                                       OtAdminService admService, OtSealService sealService,
                                       Gson g) {
    return new OtConfigService(daos, valService, admService, sealService, g);
  }

}
