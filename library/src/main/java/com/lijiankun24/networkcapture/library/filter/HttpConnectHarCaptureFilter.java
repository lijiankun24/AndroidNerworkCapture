package com.lijiankun24.networkcapture.library.filter;

import com.google.common.cache.CacheBuilder;
import com.lijiankun24.networkcapture.library.filter.support.HttpConnectTiming;
import com.lijiankun24.networkcapture.library.har.Har;

import org.littleshoot.proxy.impl.ProxyUtils;

import java.net.InetSocketAddress;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * HttpConnectHarCaptureFilter.java
 * <p>
 * Created by lijiankun on 17/11/4.
 */

public class HttpConnectHarCaptureFilter extends HttpsAwareFiltersAdapter {

    private static final int HTTP_CONNECT_TIMING_EVICTION_SECONDS = 60;

    private static final int HTTP_CONNECT_TIMING_CONCURRENCY_LEVEL = 50;

    private final Har har;

    private static final ConcurrentMap<InetSocketAddress, HttpConnectTiming> httpConnectTimes =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(HTTP_CONNECT_TIMING_EVICTION_SECONDS, TimeUnit.SECONDS)
                    .concurrencyLevel(HTTP_CONNECT_TIMING_CONCURRENCY_LEVEL)
                    .<InetSocketAddress, HttpConnectTiming>build()
                    .asMap();

    public HttpConnectHarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx, Har har, String currentPageRef) {
        super(originalRequest, ctx);

        if (har == null) {
            throw new IllegalStateException("Attempted har capture when har is null");
        }

        if (!ProxyUtils.isCONNECT(originalRequest)) {
            throw new IllegalStateException("Attempted HTTP CONNECT har capture on non-HTTP CONNECT request");
        }

        this.har = har;
    }

    public static HttpConnectTiming consumeConnectTimingForConnection(InetSocketAddress clientAddress) {
        return httpConnectTimes.remove(clientAddress);
    }

}
