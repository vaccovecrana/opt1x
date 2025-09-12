package io.vacco.opt1x.impl;

import am.ik.yavi.builder.ValidatorBuilder;
import am.ik.yavi.constraint.CharSequenceConstraint;
import am.ik.yavi.core.*;
import io.vacco.opt1x.dao.*;
import io.vacco.opt1x.dto.*;
import io.vacco.opt1x.schema.*;
import java.util.*;
import java.util.regex.Pattern;

public class OtValid {

  public static <T> CharSequenceConstraint<T, String> nnNeNb(CharSequenceConstraint<T, String> c) {
    return c.notNull().notBlank().notEmpty();
  }

  public static <T> CharSequenceConstraint<T, String> minLen(CharSequenceConstraint<T, String> c, int min) {
    return c.greaterThanOrEqual(min);
  }

  private static final Pattern keyNamePattern = Pattern.compile("^[a-zA-Z_$][a-zA-Z0-9_$\\-.]*$");
  private static final Pattern pathPattern    = Pattern.compile("^(?:/|(/[a-zA-Z0-9_\\-.]+)+/?)$");
  private static final Pattern sha256Pattern  = Pattern.compile("^[0-9a-fA-F]{64}$");

  public static <T> CharSequenceConstraint<T, String> keyName(CharSequenceConstraint<T, String> c) {
    c.predicate(
      x -> keyNamePattern.matcher(x).matches(),
      ViolationMessage.of(
        "charSequence.keyName",
        "\"{0}\" must be a valid key name (numbers, digits, allowing [-] and [.])"
      )
    );
    return c;
  }

  public static <T> CharSequenceConstraint<T, String> noNullExpression(CharSequenceConstraint<T, String> c) {
    c.predicate(v -> !v.contains("null"),
      ViolationMessage.of(
        "charSequence.hasNull",
        "\"{0}\" contains the expression 'null'"
      )
    );
    return c;
  }

  public static <T> CharSequenceConstraint<T, String> path(CharSequenceConstraint<T, String> c) {
    c.predicates().add(ConstraintPredicate.of(
      x -> pathPattern.matcher(x).matches(),
      ViolationMessage.of(
        "charSequence.path",
        "\"{0}\" must be a valid path"
      ),
      () -> new Object[] {}, NullAs.VALID
    ));
    return c;
  }

  public static <T> CharSequenceConstraint<T, String> sha256Hash(CharSequenceConstraint<T, String> c) {
    c.predicates().add(ConstraintPredicate.of(
      x -> sha256Pattern.matcher(x).matches(),
      ViolationMessage.of(
        "charSequence.sha256Hash",
        "\"{0}\" must be a valid SHA256 hash (64 hex characters)"
      ),
      () -> new Object[] {}, NullAs.VALID
    ));
    return c;
  }

  private static final Validator<OtNamespace> OtNamespaceVld = ValidatorBuilder.<OtNamespace>of()
    ._string(ns -> ns.name, OtNamespaceDao.fld_name, c -> noNullExpression(keyName(minLen(nnNeNb(c), 3))))
    ._string(ns -> ns.path, OtNamespaceDao.fld_path, c -> path(nnNeNb(c)))
    ._long(ns -> ns.createUtcMs, OtNamespaceDao.fld_createUtcMs, c -> c.greaterThan(0L))
    .build();

  private static final Validator<OtGroup> OtGroupVld = ValidatorBuilder.<OtGroup>of()
    ._string(grp -> grp.name, OtGroupDao.fld_name, c -> noNullExpression(keyName(minLen(nnNeNb(c), 3))))
    ._string(grp -> grp.path, OtGroupDao.fld_path, c -> path(nnNeNb(c)))
    ._long(grp -> grp.createUtcMs, OtGroupDao.fld_createUtcMs, c -> c.greaterThan(0L))
    .build();

  private static final Validator<OtGroupNs> OtGroupNsVld = ValidatorBuilder.<OtGroupNs>of()
    ._integer(gns -> gns.gid, OtGroupNsDao.fld_gid, Constraint::notNull)
    ._integer(gns -> gns.nsId, OtGroupNsDao.fld_nsId, Constraint::notNull)
    ._integer(gns -> gns.grantKid, OtGroupNsDao.fld_grantKid, Constraint::notNull)
    .build();

  private static final Validator<OtApiKey> OtApiKeyVld = ValidatorBuilder.<OtApiKey>of()
    ._string(k -> k.name, OtApiKeyDao.fld_name, c -> noNullExpression(keyName(minLen(nnNeNb(c), 3))))
    ._string(k -> k.path, OtApiKeyDao.fld_path, c -> noNullExpression(path(nnNeNb(c))))
    ._string(k -> k.hash, OtApiKeyDao.fld_hash, c -> sha256Hash(nnNeNb(c)))
    ._long(k -> k.createUtcMs, OtApiKeyDao.fld_createUtcMs, c -> c.greaterThan(0L))
    .build();

  private static final Validator<OtKeyGroup> OtKeyGroupVld = ValidatorBuilder.<OtKeyGroup>of()
    ._integer(kg -> kg.kid, OtKeyGroupDao.fld_kid, Constraint::notNull)
    ._integer(kg -> kg.gid, OtKeyGroupDao.fld_gid, Constraint::notNull)
    ._object(kg -> kg.role, OtKeyGroupDao.fld_role, Constraint::notNull)
    ._integer(kg -> kg.grantKid, OtKeyGroupDao.fld_grantKid, Constraint::notNull)
    ._long(kg -> kg.grantUtcMs, OtKeyGroupDao.fld_grantUtcMs, c -> c.greaterThan(0L))
    .build();

  private static final Validator<OtValue> OtValueVld = ValidatorBuilder.<OtValue>of()
    ._string(v -> v.name, OtValueDao.fld_name, c -> noNullExpression(keyName(minLen(nnNeNb(c), 3))))
    ._string(v -> v.val, OtValueDao.fld_val, OtValid::nnNeNb)
    ._long(v -> v.createUtcMs, OtValueDao.fld_createUtcMs, c -> c.greaterThan(0L))
    ._object(c -> c.type, OtValueDao.fld_type, Constraint::notNull)
    .build();

  private static final Validator<OtNode> OtNodeVld = ValidatorBuilder.<OtNode>of()
    ._integer(c -> c.cid, OtNodeDao.fld_cid, Constraint::notNull)
    ._string(c -> c.label, OtNodeDao.fld_label, c -> noNullExpression(minLen(nnNeNb(c), 1)))
    ._object(c -> c.type, OtNodeDao.fld_type, Constraint::notNull)
    .build();

  private static final Validator<OtConfig> OtConfigVld = ValidatorBuilder.<OtConfig>of()
    ._integer(c -> c.nsId, OtConfigDao.fld_nsId, Constraint::notNull)
    ._string(c -> c.name, OtConfigDao.fld_name, c -> noNullExpression(keyName(minLen(nnNeNb(c), 3))))
    .build();

  private static final Map<Class<?>, Validator<?>> validators = new HashMap<>();

  static {
    validators.put(OtGroup.class, OtGroupVld);
    validators.put(OtGroupNs.class, OtGroupNsVld);
    validators.put(OtNamespace.class, OtNamespaceVld);
    validators.put(OtApiKey.class, OtApiKeyVld);
    validators.put(OtKeyGroup.class, OtKeyGroupVld);
    validators.put(OtValue.class, OtValueVld);
    validators.put(OtNode.class, OtNodeVld);
    validators.put(OtConfig.class, OtConfigVld);
  }

  public static <T> List<OtValidation> validate(T t) {
    @SuppressWarnings("unchecked")
    var validator = (Validator<T>) validators.get(t.getClass());
    if (validator == null) {
      throw new IllegalStateException("Unknown validated type " + t.getClass());
    }
    var out = new ArrayList<OtValidation>();
    var validations = validator.validate(t);
    validations.forEach(cv ->
      out.add(OtValidation.vld(
        cv.name(), cv.message(), cv.messageKey(),
        cv.defaultMessageFormat()
      ))
    );
    return out;
  }

  public static <T, K extends OtResult> K validate(T value, K result) {
    return result.withValidations(validate(value));
  }

}