package io.vacco.opt1x.dto;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

public class OtResult {

  public String error;
  public final List<OtValidation> validations = new ArrayList<>();

  @SuppressWarnings("unchecked")
  public <T extends OtResult> T withError(String error) {
    this.error = error;
    return (T) this;
  }

  public <T extends OtResult> T withError(Exception e) {
    return withError(
      e.getMessage() != null
        ? e.getMessage()
        : e.getClass().getCanonicalName()
    );
  }

  @SuppressWarnings("unchecked")
  public <T extends OtResult> T withValidations(List<OtValidation> validations) {
    if (validations != null) {
      this.validations.clear();
      this.validations.addAll(validations);
    }
    return (T) this;
  }

  public boolean ok() {
    return error == null && validations.isEmpty();
  }

  @SuppressWarnings("unchecked")
  public <T extends OtResult> T validate(Function<T, T> validator) {
    if (ok()) {
      return validator.apply((T) this);
    }
    return (T) this;
  }

}
