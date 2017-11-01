package com.lijiankun24.networkcapture.library.filter;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * HarCaptureFilter.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HarCaptureFilter extends HttpsAwareFiltersAdapter {

    public HarCaptureFilter(HttpRequest originalRequest) {
        super(originalRequest);
    }

    public HarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx) {
        super(originalRequest, ctx);
    }
}
