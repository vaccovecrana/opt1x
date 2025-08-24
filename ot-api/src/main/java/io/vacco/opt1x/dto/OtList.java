package io.vacco.opt1x.dto;

import io.vacco.metolithe.util.MtPage1;
import java.util.Objects;

public class OtList<T, N> extends OtResult {

  public MtPage1<T, N> page;

  public OtList<T, N> withPage(MtPage1<T, N> page) {
    this.page = Objects.requireNonNull(page);
    return this;
  }

}
