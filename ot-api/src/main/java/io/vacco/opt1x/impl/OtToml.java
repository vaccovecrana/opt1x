package io.vacco.opt1x.impl;

import java.util.*;

public class OtToml {

  private static void appendValue(Object o, StringBuilder sb) {
    if (o instanceof String) {
      var s = (String) o;
      s = s
        .replace("\\", "\\\\")
        .replace("\"", "\\\"")
        .replace("\b", "\\b")
        .replace("\f", "\\f")
        .replace("\n", "\\n")
        .replace("\r", "\\r")
        .replace("\t", "\\t");
      sb.append("\"").append(s).append("\"");
    } else if (o instanceof Boolean) {
      sb.append(((Boolean) o).toString().toLowerCase());
    } else if (o instanceof Number) {
      sb.append(o);
    } else if (o instanceof List<?>) {
      var list = (List<?>) o;
      sb.append("[");
      for (int i = 0; i < list.size(); i++) {
        appendValue(list.get(i), sb);
        if (i < list.size() - 1) {
          sb.append(", ");
        }
      }
      sb.append("]");
    } else {
      throw new UnsupportedOperationException("Unsupported value type: " + (o != null ? o.getClass().getName() : "null"));
    }
  }

  @SuppressWarnings("unchecked")
  private static void render(String currentTable, Map<String, Object> data, StringBuilder sb) {
    boolean hasDirectContent = false;
    for (var v : data.values()) {
      if (!(v instanceof Map) && !(v instanceof List && !((List<?>) v).isEmpty() && ((List<?>) v).get(0) instanceof Map)) {
        hasDirectContent = true;
        break;
      }
    }
    if (hasDirectContent && !currentTable.isEmpty()) {
      sb.append("[").append(currentTable).append("]\n");
    }
    // Direct keys and inline arrays
    for (var entry : data.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      if (value instanceof Map) continue;
      if (value instanceof List && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Map) continue;
      sb.append(key).append(" = ");
      appendValue(value, sb);
      sb.append("\n");
    }
    // Array-of-tables
    for (var entry : data.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      if (value instanceof List && !((List<?>) value).isEmpty() && ((List<?>) value).get(0) instanceof Map) {
        var fullKey = currentTable.isEmpty() ? key : currentTable + "." + key;
        var list = (List<Map<String, Object>>) value;
        for (var item : list) {
          sb.append("[[").append(fullKey).append("]]\n");
          render(fullKey, item, sb);
        }
      }
    }
    // Sub-tables
    for (var entry : data.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      if (value instanceof Map) {
        var fullKey = currentTable.isEmpty() ? key : currentTable + "." + key;
        render(fullKey, (Map<String, Object>) value, sb);
      }
    }
  }

  public static String toToml(Map<String, Object> data) {
    var sb = new StringBuilder();
    render("", data, sb);
    return sb.toString();
  }

}