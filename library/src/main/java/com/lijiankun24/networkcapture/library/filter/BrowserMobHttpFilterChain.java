package com.lijiankun24.networkcapture.library.filter;

import android.annotation.TargetApi;
import android.util.Log;

import com.lijiankun24.networkcapture.library.proxy.BrowserMobProxyServer;
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

    private static final String TAG = "BrowserMobHttpFilter";

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
        for (HttpFilters filter : mFiltersList) {
            try {
                HttpResponse filterResponse = filter.clientToProxyRequest(httpObject);
                if (filterResponse != null) {
                    // if we are short-circuiting the response to an HttpRequest, update ModifiedRequestAwareFilter instances
                    // with this (possibly) modified HttpRequest before returning the short-circuit response

                    return filterResponse;
                }
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e.getMessage());
            }
        }
        return super.clientToProxyRequest(httpObject);
    }

    @Override
    public HttpResponse proxyToServerRequest(HttpObject httpObject) {
        for (HttpFilters filter : mFiltersList) {
            try {
                HttpResponse filterResponse = filter.proxyToServerRequest(httpObject);
                if (filterResponse != null) {
                    return filterResponse;
                }
            } catch (RuntimeException e) {
                Log.i(TAG, "Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
        return null;
    }

    @Override
    public void proxyToServerRequestSending() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerRequestSending();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void proxyToServerRequestSent() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerRequestSent();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        HttpObject processedHttpObject = httpObject;

        for (HttpFilters filter : mFiltersList) {
            try {
                processedHttpObject = filter.serverToProxyResponse(processedHttpObject);
                if (processedHttpObject == null) {
                    return null;
                }
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
        return processedHttpObject;
    }

    @Override
    public void serverToProxyResponseTimedOut() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.serverToProxyResponseTimedOut();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void serverToProxyResponseReceiving() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.serverToProxyResponseReceiving();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void serverToProxyResponseReceived() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.serverToProxyResponseReceived();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public HttpObject proxyToClientResponse(HttpObject httpObject) {
        HttpObject processedHttpObject = httpObject;
        for (HttpFilters filter : mFiltersList) {
            try {
                processedHttpObject = filter.proxyToClientResponse(processedHttpObject);
                if (processedHttpObject == null) {
                    return null;
                }
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }

        return processedHttpObject;
    }

    @Override
    public void proxyToServerConnectionQueued() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerConnectionQueued();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @TargetApi(19)
    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        InetSocketAddress overrideAddress = null;
        String newServerHostAndPort = resolvingServerHostAndPort;

        for (HttpFilters filter : mFiltersList) {
            try {
                InetSocketAddress filterResult = filter.proxyToServerResolutionStarted(newServerHostAndPort);
                if (filterResult != null) {
                    overrideAddress = filterResult;
                    newServerHostAndPort = filterResult.getHostString() + ":" + filterResult.getPort();
                }
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }

        return overrideAddress;
    }

    @Override
    public void proxyToServerResolutionFailed(String hostAndPort) {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerResolutionFailed(hostAndPort);
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerResolutionSucceeded(serverHostAndPort, resolvedRemoteAddress);
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }

        super.proxyToServerResolutionSucceeded(serverHostAndPort, resolvedRemoteAddress);
    }

    @Override
    public void proxyToServerConnectionStarted() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerConnectionStarted();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void proxyToServerConnectionSSLHandshakeStarted() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerConnectionSSLHandshakeStarted();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void proxyToServerConnectionFailed() {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerConnectionFailed();
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }

    @Override
    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
        for (HttpFilters filter : mFiltersList) {
            try {
                filter.proxyToServerConnectionSucceeded(serverCtx);
            } catch (RuntimeException e) {
                L.w("Filter in filter chain threw exception. Filter method may have been aborted." + e);
            }
        }
    }
}
