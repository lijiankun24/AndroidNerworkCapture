package com.lijiankun24.networkcapture.library.util;

import com.google.common.net.HostAndPort;

import java.util.Map;

import io.netty.handler.codec.http.HttpHeaders;

/**
 * Utility class with static methods for processing HTTP requests and responses.
 */
public class BrowserMobHttpUtil {

    public static String removeMatchingPort(String hostWithPort, int portNumber) {
        HostAndPort parsedHostAndPort = HostAndPort.fromString(hostWithPort);
        if (parsedHostAndPort.hasPort() && parsedHostAndPort.getPort() == portNumber) {
            return HostAndPort.fromHost(parsedHostAndPort.getHostText()).toString();
        } else {
            return hostWithPort;
        }
    }

    public static long getHeaderSize(HttpHeaders headers) {
        long headersSize = 0;
        for (Map.Entry<String, String> header : headers.entries()) {
            // +2 for ': ', +2 for new line
            headersSize += header.getKey().length() + header.getValue().length() + 4;
        }
        return headersSize;
    }
}
