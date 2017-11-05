package com.lijiankun24.networkcapture.library.util;

import com.google.common.net.HostAndPort;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Locale;

import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpRequest;

/**
 * Contains utility methods for netty {@link HttpRequest} and related objects.
 */
public class HttpUtil {

    public static boolean startsWithHttpOrHttps(String uri) {
        if (uri == null) {
            return false;
        }

        String lowercaseUri = uri.toLowerCase(Locale.US);

        return lowercaseUri.startsWith("http://") || lowercaseUri.startsWith("https://");
    }

    public static String getHostAndPortFromRequest(HttpRequest httpRequest) {
        if (startsWithHttpOrHttps(httpRequest.getUri())) {
            try {
                return getHostAndPortFromUri(httpRequest.getUri());
            } catch (URISyntaxException e) {
                // the URI could not be parsed, so return the host and port in the Host header
            }
        }

        return parseHostHeader(httpRequest, true);
    }

    public static String getHostAndPortFromUri(String uriString) throws URISyntaxException {
        URI uri = new URI(uriString);
        if (uri.getPort() == -1) {
            return uri.getHost();
        } else {
            return HostAndPort.fromParts(uri.getHost(), uri.getPort()).toString();
        }
    }

    private static String parseHostHeader(HttpRequest httpRequest, boolean includePort) {
        // this header parsing logic is adapted from ClientToProxyConnection#identifyHostAndPort.
        List<String> hosts = httpRequest.headers().getAll(HttpHeaders.Names.HOST);
        if (!hosts.isEmpty()) {
            String hostAndPort = hosts.get(0);

            if (includePort) {
                return hostAndPort;
            } else {
                HostAndPort parsedHostAndPort = HostAndPort.fromString(hostAndPort);
                return parsedHostAndPort.getHostText();
            }
        } else {
            return null;
        }
    }

    public static String getHostFromRequest(HttpRequest httpRequest) {
        // try to use the URI from the request first, if the URI starts with http:// or https://. checking for http/https avoids confusing
        // java's URI class when the request is for a malformed URL like '//some-resource'.
        String host = null;
        if (startsWithHttpOrHttps(httpRequest.getUri())) {
            try {
                URI uri = new URI(httpRequest.getUri());
                host = uri.getHost();
            } catch (URISyntaxException e) {
            }
        }

        // if there was no host in the URI, attempt to grab the host from the Host header
        if (host == null || host.isEmpty()) {
            host = parseHostHeader(httpRequest, false);
        }

        return host;
    }
}
