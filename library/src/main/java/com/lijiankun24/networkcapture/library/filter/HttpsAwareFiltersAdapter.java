package com.lijiankun24.networkcapture.library.filter;

import com.google.common.net.HostAndPort;
import com.lijiankun24.networkcapture.library.util.BrowserMobHttpUtil;
import com.lijiankun24.networkcapture.library.util.HttpUtil;

import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.impl.ProxyUtils;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.util.Attribute;
import io.netty.util.AttributeKey;

/**
 * HttpsAwareFiltersAdapter.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HttpsAwareFiltersAdapter extends HttpFiltersAdapter {

    public static final String IS_HTTPS_ATTRIBUTE_NAME = "isHttps";

    public static final String HOST_ATTRIBUTE_NAME = "host";

    public HttpsAwareFiltersAdapter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    public HttpsAwareFiltersAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }

    public String getFullUrl(HttpRequest modifiedRequest) {
        if (ProxyUtils.isCONNECT(modifiedRequest)) {
            String hostNoDefaultPort = BrowserMobHttpUtil.removeMatchingPort(modifiedRequest.getUri(), 443);
            return "https://" + hostNoDefaultPort;
        }

        if (HttpUtil.startsWithHttpOrHttps(modifiedRequest.getUri())) {
            return modifiedRequest.getUri();
        }

        String hostAndPort = getHostAndPort(modifiedRequest);
        String path = modifiedRequest.getUri();
        String url;
        if (isHttps()) {
            url = "https://" + hostAndPort + path;
        } else {
            url = "http://" + hostAndPort + path;
        }
        return url;
    }

    public String getHostAndPort(HttpRequest modifiedRequest) {
        if (isHttps()) {
            return getHttpsRequestHostAndPort();
        } else {
            return HttpUtil.getHostAndPortFromRequest(modifiedRequest);
        }
    }

    private String getHttpsRequestHostAndPort() throws IllegalStateException {
        if (!isHttps()) {
            throw new IllegalStateException("Request is not HTTPS. Cannot get host and port on non-HTTPS request using this method.");
        }

        Attribute<String> hostnameAttr = ctx.attr(AttributeKey.<String>valueOf(HOST_ATTRIBUTE_NAME));
        return hostnameAttr.get();
    }

    public boolean isHttps() {
        Attribute<Boolean> isHttpsAttr = ctx.attr(AttributeKey.<Boolean>valueOf(IS_HTTPS_ATTRIBUTE_NAME));

        Boolean isHttps = isHttpsAttr.get();
        if (isHttps == null) {
            return false;
        } else {
            return isHttps;
        }
    }

    public String getHost(HttpRequest modifiedRequest) {
        String serverHost;
        if (isHttps()) {
            HostAndPort hostAndPort = HostAndPort.fromString(getHttpsRequestHostAndPort());
            serverHost = hostAndPort.getHostText();
        } else {
            serverHost = HttpUtil.getHostFromRequest(modifiedRequest);
        }
        return serverHost;
    }
}
