package com.lijiankun24.networkcapture.library.filter;

import org.littleshoot.proxy.HttpFiltersAdapter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * HttpsAwareFiltersAdapter.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HttpsAwareFiltersAdapter extends HttpFiltersAdapter {

    public HttpsAwareFiltersAdapter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    public HttpsAwareFiltersAdapter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }
}
