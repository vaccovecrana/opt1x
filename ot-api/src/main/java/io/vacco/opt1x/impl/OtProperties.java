package io.vacco.opt1x.impl;

import java.util.*;

public class OtProperties {

  private static String escapeKey(String key) {
    return key.replace("\\", "\\\\")
      .replace("=", "\\=")
      .replace(":", "\\:")
      .replace("#", "\\#")
      .replace("!", "\\!")
      .replace(" ", "\\ ");
  }

  private static String escapeValue(String value) {
    return value.replace("\\", "\\\\")
      .replace("=", "\\=")
      .replace(":", "\\:")
      .replace("#", "\\#")
      .replace("!", "\\!")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }

  private static void appendValue(Object value, StringBuilder sb) {
    if (value instanceof String) {
      sb.append(escapeValue((String) value));
    } else if (value instanceof Boolean || value instanceof Number) {
      sb.append(value);
    } else {
      throw new UnsupportedOperationException("Unsupported value type: " + (value != null ? value.getClass().getName() : "null"));
    }
  }

  private static void appendList(List<Object> list, StringBuilder sb) {
    for (int i = 0; i < list.size(); i++) {
      appendValue(list.get(i), sb);
      if (i < list.size() - 1) {
        sb.append(",");
      }
    }
  }

  @SuppressWarnings("unchecked")
  private static void flatten(String prefix, Map<String, Object> map, StringBuilder sb) {
    for (var entry : map.entrySet()) {
      var key = entry.getKey();
      var value = entry.getValue();
      var fullKey = prefix.isEmpty() ? key : prefix + "." + key;
      if (value instanceof Map) {
        flatten(fullKey, (Map<String, Object>) value, sb);
      } else if (value instanceof List) {
        var list = (List<?>) value;
        if (!list.isEmpty() && list.get(0) instanceof Map) {
          for (int i = 0; i < list.size(); i++) {
            flatten(fullKey + "[" + i + "]", (Map<String, Object>) list.get(i), sb);
          }
        } else {
          sb.append(escapeKey(fullKey)).append("=");
          appendList((List<Object>) value, sb);
          sb.append("\n");
        }
      } else {
        sb.append(escapeKey(fullKey)).append("=");
        appendValue(value, sb);
        sb.append("\n");
      }
    }
  }

  public static String toProperties(Map<String, Object> data) {
    var sb = new StringBuilder();
    flatten("", data, sb);
    return sb.toString();
  }

}
