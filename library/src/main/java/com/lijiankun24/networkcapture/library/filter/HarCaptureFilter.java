package com.lijiankun24.networkcapture.library.filter;

import android.annotation.TargetApi;

import com.lijiankun24.networkcapture.library.filter.support.HttpConnectTiming;
import com.lijiankun24.networkcapture.library.filter.util.HarCaptureUtil;
import com.lijiankun24.networkcapture.library.har.Har;
import com.lijiankun24.networkcapture.library.har.HarEntry;
import com.lijiankun24.networkcapture.library.har.HarNameValuePair;
import com.lijiankun24.networkcapture.library.har.HarRequest;
import com.lijiankun24.networkcapture.library.har.HarResponse;
import com.lijiankun24.networkcapture.library.util.BrowserMobHttpUtil;

import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;
import io.netty.handler.codec.http.QueryStringDecoder;

/**
 * HarCaptureFilter.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HarCaptureFilter extends HttpsAwareFiltersAdapter {

    private final Har har;

    private final HarEntry harEntry;

    private final InetSocketAddress clientAddress;

    public HarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx, Har har) {
        super(originalRequest, ctx);
        this.har = har;
        this.clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        harEntry = new HarEntry("pageref");
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            har.getHarLog().addEntry(harEntry);
            HttpRequest httpRequest = (HttpRequest) httpObject;
            HarRequest request = createHarRequestForHttpRequest(httpRequest);
            harEntry.setRequest(request);
            HarResponse defaultHarResponse = HarCaptureUtil.createHarResponseForFailure();
            defaultHarResponse.setError(HarCaptureUtil.getNoResponseReceivedErrorMessage());
            harEntry.setResponse(defaultHarResponse);

            captureQueryParameters(httpRequest);

            captureRequestHeaderSize(httpRequest);

            captureConnectTiming();
        }
        return super.clientToProxyRequest(httpObject);
    }

    private HarRequest createHarRequestForHttpRequest(HttpRequest httpRequest) {
        String url = getFullUrl(httpRequest);

        return new HarRequest(httpRequest.getMethod().toString(), url, httpRequest.getProtocolVersion().text());
    }

    @TargetApi(19)
    protected void captureQueryParameters(HttpRequest httpRequest) {
        QueryStringDecoder queryStringDecoder = new QueryStringDecoder(httpRequest.getUri(), StandardCharsets.UTF_8);

        try {
            for (Map.Entry<String, List<String>> entry : queryStringDecoder.parameters().entrySet()) {
                for (String value : entry.getValue()) {
                    harEntry.getRequest().getQueryString().add(new HarNameValuePair(entry.getKey(), value));
                }
            }
        } catch (IllegalArgumentException e) {
        }
    }

    protected void captureRequestHeaderSize(HttpRequest httpRequest) {
        String requestLine = httpRequest.getMethod().toString() + ' ' + httpRequest.getUri() + ' ' + httpRequest.getProtocolVersion().toString();
        // +2 => CRLF after status line, +4 => header/data separation
        long requestHeadersSize = requestLine.length() + 6;

        HttpHeaders headers = httpRequest.headers();
        requestHeadersSize += BrowserMobHttpUtil.getHeaderSize(headers);

        harEntry.getRequest().setHeadersSize(requestHeadersSize);
    }

    protected void captureConnectTiming() {
        HttpConnectTiming httpConnectTiming = HttpConnectHarCaptureFilter.consumeConnectTimingForConnection(clientAddress);
        if (httpConnectTiming != null) {
            harEntry.getTimings().setSsl(httpConnectTiming.getSslHandshakeTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setConnect(httpConnectTiming.getConnectTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setBlocked(httpConnectTiming.getBlockedTimeNanos(), TimeUnit.NANOSECONDS);
            harEntry.getTimings().setDns(httpConnectTiming.getDnsTimeNanos(), TimeUnit.NANOSECONDS);
        }
    }
}
