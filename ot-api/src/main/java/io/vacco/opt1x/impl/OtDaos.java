package io.vacco.opt1x.impl;

import io.vacco.metolithe.id.MtMurmur3IFn;
import io.vacco.metolithe.query.MtJdbc;
import io.vacco.opt1x.dao.*;

import static io.vacco.opt1x.schema.OtSchema.*;

public class OtDaos {

  public final OtApiKeyDao        akd;
  public final OtKeyNamespaceDao  knd;
  public final OtNamespaceDao     nsd;
  public final OtValueDao         vld;
  public final OtConfigDao        cfd;
  public final OtNodeDao          ndd;

  public OtDaos(MtJdbc db, String schema) {
    var idFn = new MtMurmur3IFn((int) OtOptions.dbSeed);
    this.akd = new OtApiKeyDao(schema, Fmt, db, idFn);
    this.knd = new OtKeyNamespaceDao(schema, Fmt, db, idFn);
    this.nsd = new OtNamespaceDao(schema, Fmt, db, idFn);
    this.vld = new OtValueDao(schema, Fmt, db, idFn);
    this.cfd = new OtConfigDao(schema, Fmt, db, idFn);
    this.ndd = new OtNodeDao(schema, Fmt, db, idFn);
  }

}
