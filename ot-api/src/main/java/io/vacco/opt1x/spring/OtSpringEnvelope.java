package io.vacco.opt1x.spring;

import io.vacco.opt1x.dto.OtConfigOp;
import io.vacco.opt1x.impl.OtRender;
import io.vacco.opt1x.schema.OtNamespace;
import java.util.*;

public class OtSpringEnvelope {

  private final OtSpringApp      app;
  private final OtNamespace      appNs;
  private final List<OtConfigOp> configs;

  public OtSpringEnvelope(OtSpringApp app, OtNamespace appNs, List<OtConfigOp> configs) {
    this.app = Objects.requireNonNull(app, "OtSpringApp cannot be null");
    this.appNs = Objects.requireNonNull(appNs, "OtNamespace cannot be null");
    this.configs = Objects.requireNonNull(configs, "Profile configs cannot be null");
  }

  @SuppressWarnings("unchecked")
  private static void flattenMap(String prefix, Map<String, Object> input, Map<String, Object> output) {
    for (var entry : input.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      var fullKey = prefix.isEmpty() ? key : prefix + "." + key;
      if (value instanceof Map) {
        flattenMap(fullKey, (Map<String, Object>) value, output);
      } else {
        output.put(fullKey, value);
      }
    }
  }

  private static Map<String, Object> toFlatMap(List<io.vacco.opt1x.dto.OtVar> vars) {
    var rawMap = OtRender.toMap(vars);
    var flatMap = new LinkedHashMap<String, Object>();
    flattenMap("", rawMap, flatMap);
    return flatMap;
  }

  public void populate() {
    if (app.propertySources == null) {
      app.propertySources = new ArrayList<>();
    } else {
      app.propertySources.clear();
    }
    app.label = appNs.path;
    app.version = Long.toHexString(appNs.createUtcMs); // TODO this needs tweaks.

    var orderedConfigs = new ArrayList<>(configs);
    Collections.reverse(orderedConfigs);

    for (var configOp : orderedConfigs) {
      if (configOp.cfg == null || configOp.cfg.name == null) {
        continue;
      }
      var propSrc = new OtPropSrc();
      propSrc.name = String.format("%s/%s", appNs.path, configOp.cfg.name);
      propSrc.source = toFlatMap(configOp.vars);
      app.propertySources.add(propSrc);
    }
  }
}
