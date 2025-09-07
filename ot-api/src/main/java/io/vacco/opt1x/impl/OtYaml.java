package io.vacco.opt1x.impl;

import java.util.*;

public class OtYaml {

  private static boolean needsQuotes(String s) {
    if (s.isEmpty()) return true;
    char first = s.charAt(0);
    if (Character.isDigit(first) || first == '-' || first == '+' || s.equals("true") || s.equals("false") || s.equals("null")) {
      return true;
    }
    return s.contains(":") || s.contains(" ") || s.contains("\n") || s.contains("\"") || s.contains("'") || s.contains("[") || s.contains("{") || s.contains("#") || s.contains("&") || s.contains("*") || s.contains("!") || s.contains("%") || s.contains("@") || s.contains(",") || s.contains("]") || s.contains("}") || s.contains("?") || s.contains(">") || s.contains("<") || s.contains("=") || s.contains("`") || s.contains("\\") || s.contains("|") || s.contains("~") || s.contains("^");
  }

  private static String escapeString(String s) {
    return s.replace("\\", "\\\\")
      .replace("\"", "\\\"")
      .replace("\b", "\\b")
      .replace("\f", "\\f")
      .replace("\n", "\\n")
      .replace("\r", "\\r")
      .replace("\t", "\\t");
  }

  private static String escapeKey(String key) {
    if (needsQuotes(key)) {
      return "\"" + escapeString(key) + "\"";
    }
    return key;
  }

  @SuppressWarnings("unchecked")
  private static void renderValue(Object value, StringBuilder sb) {
    if (value instanceof String) {
      var s = (String) value;
      boolean needsQuotes = needsQuotes(s);
      if (needsQuotes) {
        sb.append("\"").append(escapeString(s)).append("\"");
      } else {
        sb.append(s);
      }
    } else if (value instanceof Boolean) {
      sb.append(value);
    } else if (value instanceof Number) {
      sb.append(value);
    } else if (value instanceof List) {
      renderList((List<Object>) value, sb); // Inline nested lists
    } else if (value instanceof Map) {
      sb.append("{");
      var it = ((Map<String, Object>) value).entrySet().iterator();
      while (it.hasNext()) {
        var e = it.next();
        sb.append(escapeKey(e.getKey())).append(": ");
        renderValue(e.getValue(), sb);
        if (it.hasNext()) {
          sb.append(", ");
        }
      }
      sb.append("}");
    } else {
      throw new UnsupportedOperationException(
        "Unsupported value type: " + (value != null ? value.getClass().getName() : "null")
      );
    }
  }

  private static void renderList(List<Object> list, StringBuilder sb) {
    sb.append("[");
    for (int i = 0; i < list.size(); i++) {
      var item = list.get(i);
      renderValue(item, sb);
      if (i < list.size() - 1) {
        sb.append(", ");
      }
    }
    sb.append("]");
  }

  private static void indent(StringBuilder sb, int indent) {
    sb.append(" ".repeat(Math.max(0, indent)));
  }

  @SuppressWarnings("unchecked")
  private static void renderMap(Map<String, Object> map, StringBuilder sb, int indent) {
    for (var entry : map.entrySet()) {
      indent(sb, indent);
      sb.append(escapeKey(entry.getKey())).append(":");
      var value = entry.getValue();
      if (value instanceof Map) {
        sb.append("\n");
        renderMap((Map<String, Object>) value, sb, indent + 2);
      } else {
        sb.append(" ");
        if (value instanceof List) {
          renderList((List<Object>) value, sb);
        } else {
          renderValue(value, sb);
        }
        sb.append("\n");
      }
    }
  }

  public static String toYaml(Map<String, Object> data) {
    var sb = new StringBuilder();
    renderMap(data, sb, 0);
    return sb.toString();
  }

}