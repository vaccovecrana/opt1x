package io.vacco.opt1x.dto;

import io.vacco.murmux.http.MxExchange;
import io.vacco.opt1x.schema.OtConstants;
import java.util.*;

public class OtRequest {

  public Map<String, String>       queryParams = new HashMap<>();
  public Map<String, List<String>> headers = new HashMap<>();

  public String userAgent;
  public String host;
  public String protocol;

  public String remoteHost;
  public String remoteAddress;
  public int    remotePort;

  public static OtRequest from(MxExchange ex) {
    var r = new OtRequest();
    r.queryParams.putAll(ex.queries);
    r.userAgent = ex.getUserAgent();
    r.host = ex.getHost();
    r.protocol = ex.getProtocol();
    ex.io.getRequestHeaders().forEach((k, v) -> {
      if (!k.contains(OtConstants.kCookie)) {
        r.headers.put(k, new ArrayList<>(v));
      }
    });

    var addr = ex.io.getRemoteAddress();
    r.remoteHost = addr.getHostName();
    r.remotePort = addr.getPort();
    r.remoteAddress = addr.getAddress().getHostAddress();
    return r;
  }

}