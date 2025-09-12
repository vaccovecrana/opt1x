package io.vacco.opt1x.spring;

import io.vacco.opt1x.schema.OtNodeFormat;
import java.util.*;

import static java.lang.String.format;
import static io.vacco.opt1x.schema.OtConstants.*;

public class OtSpringApp {

  public String          name;  // e.g., "flooper" or "application"
  public List<String>    profiles;     // e.g., ["dev", "stage"] or ["default"]
  public OtNodeFormat    format;       // e.g., "json", "yaml", "properties"
  public String          label;        // e.g., "main" or null if not provided
  public String          version;
  public List<OtPropSrc> propertySources;

  private static String[] splitMin(String in, String regex, int min) {
    if (in == null) {
      throw new IllegalArgumentException(format("Input missing for [%d] expected elements", min));
    }
    var out = in.split(regex);
    if (out.length < min) {
      throw new IllegalArgumentException(format("Input [%s] with [%d] elements, expected [%d]", in, out.length, min));
    }
    return out;
  }

  private static String notEmpty(String in) {
    if (in == null || in.isEmpty()) {
      throw new IllegalArgumentException("empty input");
    }
    return in;
  }

  private static OtNodeFormat formatOf(String raw) {
    switch (raw) {
      case "json":       return OtNodeFormat.json;
      case "yaml":
      case "yml":        return OtNodeFormat.yaml;
      case "properties": return OtNodeFormat.props;
    }
    throw new IllegalArgumentException(format("Unsupported output format: %s", raw));
  }

  private static void setProfilesAndFormat(String in, OtSpringApp app) {
    var l1 = splitMin(in, "\\.", 1);
    var profiles = new ArrayList<String>();
    for (var prof : splitMin(l1[0], ",", 1)) {
      if (!prof.isEmpty()) {
        profiles.add(prof);
      }
    }
    app.profiles = profiles;
    app.format = l1.length == 1 ? OtNodeFormat.json : formatOf(notEmpty(l1[1]));
  }

  private static OtSpringApp parse1(String s0) {
    var out = new OtSpringApp();
    var l0 = splitMin(s0, "-", 2);
    out.name = notEmpty(l0[0]);
    setProfilesAndFormat(l0[1], out);
    return out;
  }

  private static OtSpringApp parse2(String s0, String s1) {
    var out = new OtSpringApp();
    out.name = notEmpty(s0);
    setProfilesAndFormat(s1, out);
    return out;
  }

  private static OtSpringApp parse3(String s0, String s1, String s2) {
    var out = new OtSpringApp();
    out.name = notEmpty(s0);
    setProfilesAndFormat(s1, out);
    out.label = notEmpty(s2);
    return out;
  }

  public static OtSpringApp parse(String path) {
    if (path == null || !path.startsWith(springRoot) || path.length() <= 8 || path.endsWith("/")) {
      throw new IllegalArgumentException("Invalid path: Must start with /spring/, have valid segments, with no trailing slashes");
    }
    var segs = splitMin(path.substring(8), "/", 1);
    if (segs.length == 1) {
      return parse1(segs[0]);
    }
    if (segs.length == 2) {
      return parse2(segs[0], segs[1]);
    }
    if (segs.length == 3) {
      return parse3(segs[0], segs[1], segs[2]);
    }
    throw new IllegalArgumentException(format("Invalid path: [%s]", path));
  }

  @Override public String toString() {
    return format("%s %s %s %s", name, profiles, format, label);
  }

}