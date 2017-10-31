package com.lijiankun24.networkcapture.library;

import com.lijiankun24.networkcapture.library.util.L;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersAdapter;
import org.littleshoot.proxy.HttpFiltersSource;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * BrowserMobHttpFilterChain.java
 * <p>
 * Created by lijiankun on 17/10/30.
 */

public class BrowserMobHttpFilterChain extends HttpFiltersAdapter {

    private static final String TAG = "BrowserMobHttpFilterChain";

    private final BrowserMobProxyServer mProxyServer;

    private final List<HttpFilters> mFiltersList;

    public BrowserMobHttpFilterChain(HttpRequest originalRequest, ChannelHandlerContext ctx,
                                     BrowserMobProxyServer proxyServer) {
        super(originalRequest, ctx);
        mProxyServer = proxyServer;
        if (proxyServer.getFilterFactories() != null) {
            mFiltersList = new ArrayList<>(proxyServer.getFilterFactories().size());
            for (HttpFiltersSource source : proxyServer.getFilterFactories()) {
                HttpFilters httpFilters = source.filterRequest(originalRequest, ctx);
                if (httpFilters != null) {
                    mFiltersList.add(httpFilters);
                }
            }
        } else {
            mFiltersList = Collections.emptyList();
        }
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        L.i("clientToProxyRequest");
        return super.clientToProxyRequest(httpObject);
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        L.i("proxyToServerRequest");
        return super.proxyToServerRequest(httpObject);
    }

    @Override
    public void proxyToServerRequestSending() {
        L.i("proxyToServerRequestSending");
        super.proxyToServerRequestSending();
    }

    @Override
    public void proxyToServerRequestSent() {
        L.i("proxyToServerRequestSent");
        super.proxyToServerRequestSent();
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        L.i("serverToProxyResponse");
        return super.serverToProxyResponse(httpObject);
    }

    @Override
    public void serverToProxyResponseTimedOut() {
        L.i("serverToProxyResponseTimedOut");
        super.serverToProxyResponseTimedOut();
    }

    @Override
    public void serverToProxyResponseReceiving() {
        L.i("serverToProxyResponseReceiving");
        super.serverToProxyResponseReceiving();
    }

    @Override
    public void serverToProxyResponseReceived() {
        L.i("serverToProxyResponseReceived");
        super.serverToProxyResponseReceived();
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        L.i("proxyToClientResponse");
        return super.proxyToClientResponse(httpObject);
    }

    @Override
    public void proxyToServerConnectionQueued() {
        L.i("proxyToServerConnectionQueued");
        super.proxyToServerConnectionQueued();
    }

    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        L.i("proxyToServerResolutionStarted");
        return super.proxyToServerResolutionStarted(resolvingServerHostAndPort);
    }

    @Override
    public void proxyToServerResolutionFailed(String hostAndPort) {
        L.i("proxyToServerResolutionFailed");
        super.proxyToServerResolutionFailed(hostAndPort);
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
        L.i("proxyToServerResolutionSucceeded");
        super.proxyToServerResolutionSucceeded(serverHostAndPort, resolvedRemoteAddress);
    }

    @Override
    public void proxyToServerConnectionStarted() {
        L.i("proxyToServerConnectionStarted");
        super.proxyToServerConnectionStarted();
    }

    @Override
    public void proxyToServerConnectionSSLHandshakeStarted() {
        L.i("proxyToServerConnectionSSLHandshakeStarted");
        super.proxyToServerConnectionSSLHandshakeStarted();
    }

    @Override
    public void proxyToServerConnectionFailed() {
        L.i("proxyToServerConnectionFailed");
        super.proxyToServerConnectionFailed();
    }

    @Override
    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
        L.i("proxyToServerConnectionSucceeded");
        super.proxyToServerConnectionSucceeded(serverCtx);
    }
}
