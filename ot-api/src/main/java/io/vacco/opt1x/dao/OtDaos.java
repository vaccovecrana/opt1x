package io.vacco.opt1x.dao;

import io.vacco.metolithe.id.MtMurmur3IFn;
import io.vacco.metolithe.query.*;
import io.vacco.opt1x.dto.OtResult;
import io.vacco.opt1x.impl.OtOptions;

import static java.util.stream.Collectors.toList;
import static io.vacco.opt1x.dto.OtValidation.vld;
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
  public final OtValueVerDao  vvd;

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
    this.vvd = new OtValueVerDao(schema, Fmt, db, idFn);
  }

  public void onTxResult(OtResult result, MtTx tx) {
    if (tx.error != null) {
      result.withError(tx.error);
    }
    result.validations.addAll(
      tx.warnings.stream()
        .map(w -> vld(null, w.getMessage(), null, null))
        .collect(toList())
    );
  }

  public String likeFmt(String in) {
    return String.format("%s%%", in);
  }

}
