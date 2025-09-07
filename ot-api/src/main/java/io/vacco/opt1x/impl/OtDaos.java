package io.vacco.opt1x.impl;

import io.vacco.metolithe.id.MtMurmur3IFn;
import io.vacco.metolithe.query.MtJdbc;
import io.vacco.opt1x.dao.*;
import java.sql.Connection;

import static io.vacco.opt1x.impl.OtOptions.onError;
import static io.vacco.opt1x.schema.OtSchema.*;

public class OtDaos {

  public final OtApiKeyDao    akd;
  public final OtConfigDao    cfd;
  public final OtGroupDao     grd;
  public final OtGroupNsDao   gnd;
  public final OtKeyGroupDao  kgd;
  public final OtNamespaceDao nsd;
  public final OtNodeDao      ndd;
  public final OtValueDao     vld;

  public OtDaos(MtJdbc db, String schema) {
    var idFn = new MtMurmur3IFn((int) OtOptions.dbSeed);
    this.akd = new OtApiKeyDao(schema, Fmt, db, idFn);
    this.cfd = new OtConfigDao(schema, Fmt, db, idFn);
    this.grd = new OtGroupDao(schema, Fmt, db, idFn);
    this.gnd = new OtGroupNsDao(schema, Fmt, db, idFn);
    this.kgd = new OtKeyGroupDao(schema, Fmt, db, idFn);
    this.nsd = new OtNamespaceDao(schema, Fmt, db, idFn);
    this.vld = new OtValueDao(schema, Fmt, db, idFn);
    this.ndd = new OtNodeDao(schema, Fmt, db, idFn);
  }

  public String txWarningsOf(Connection conn) {
    try {
      var warn = conn.getWarnings();
      var sb = new StringBuilder();
      while (warn != null) {
        sb.append(warn.getMessage()).append("\n");
        warn = warn.getNextWarning();
      }
      if (!sb.toString().isEmpty()) {
        return sb.toString();
      }
      if (conn.getWarnings() != null) {
        conn.clearWarnings();
      }
    } catch (Exception e) {
      onError("TX warning read error", e);
      return e.getMessage();
    }
    return null;
  }

  public String likeFmt(String in) {
    return String.format("%s%%", in);
  }

}
