package com.lijiankun24.networkcapture.library.filter;

import android.util.Log;

import com.google.common.cache.CacheBuilder;
import com.lijiankun24.networkcapture.library.filter.support.HttpConnectTiming;
import com.lijiankun24.networkcapture.library.filter.util.HarCaptureUtil;
import com.lijiankun24.networkcapture.library.har.Har;
import com.lijiankun24.networkcapture.library.har.HarEntry;
import com.lijiankun24.networkcapture.library.har.HarRequest;
import com.lijiankun24.networkcapture.library.har.HarResponse;
import com.lijiankun24.networkcapture.library.har.HarTimings;
import com.lijiankun24.networkcapture.library.util.HttpUtil;

import org.littleshoot.proxy.impl.ProxyUtils;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.Date;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.TimeUnit;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpObject;
import io.netty.handler.codec.http.HttpRequest;
import io.netty.handler.codec.http.HttpResponse;

/**
 * HttpConnectHarCaptureFilter.java
 * <p>
 * Created by lijiankun on 17/11/4.
 */

public class HttpConnectHarCaptureFilter extends HttpsAwareFiltersAdapter {

    private static final String TAG = "HttpConnectHarCapture";

    private static final int HTTP_CONNECT_TIMING_EVICTION_SECONDS = 60;

    private static final int HTTP_CONNECT_TIMING_CONCURRENCY_LEVEL = 50;

    private volatile long dnsResolutionStartedNanos;

    private volatile long dnsResolutionFinishedNanos;

    private volatile long connectionQueuedNanos;

    private volatile long connectionStartedNanos;

    private volatile long connectionSucceededTimeNanos;

    private volatile long sslHandshakeStartedNanos;

    private volatile long sendStartedNanos;

    private volatile long sendFinishedNanos;

    private volatile long responseReceiveStartedNanos;

    private volatile InetAddress resolvedAddress;

    private final HttpConnectTiming httpConnectTiming;

    private final InetSocketAddress clientAddress;

    private volatile Date requestStartTime;

    private volatile HttpRequest modifiedHttpRequest;

    private final Har har;

    private static final ConcurrentMap<InetSocketAddress, HttpConnectTiming> httpConnectTimes =
            CacheBuilder.newBuilder()
                    .expireAfterWrite(HTTP_CONNECT_TIMING_EVICTION_SECONDS, TimeUnit.SECONDS)
                    .concurrencyLevel(HTTP_CONNECT_TIMING_CONCURRENCY_LEVEL)
                    .<InetSocketAddress, HttpConnectTiming>build()
                    .asMap();

    public HttpConnectHarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx, Har har) {
        super(originalRequest, ctx);

        if (har == null) {
            throw new IllegalStateException("Attempted har capture when har is null");
        }

        if (!ProxyUtils.isCONNECT(originalRequest)) {
            throw new IllegalStateException("Attempted HTTP CONNECT har capture on non-HTTP CONNECT request");
        }
        this.har = har;
        clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        httpConnectTiming = new HttpConnectTiming();
        httpConnectTimes.put(clientAddress, httpConnectTiming);
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            // store the CONNECT start time in case of failure, so we can populate the HarEntry with it
            requestStartTime = new Date();
        }
        return super.clientToProxyRequest(httpObject);
    }

    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        dnsResolutionStartedNanos = System.nanoTime();

        if (connectionQueuedNanos > 0L) {
            httpConnectTiming.setBlockedTimeNanos(dnsResolutionStartedNanos - connectionQueuedNanos);
        } else {
            httpConnectTiming.setBlockedTimeNanos(0L);
        }

        return null;
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
        this.dnsResolutionFinishedNanos = System.nanoTime();

        if (dnsResolutionStartedNanos > 0L) {
            httpConnectTiming.setDnsTimeNanos(dnsResolutionFinishedNanos - dnsResolutionStartedNanos);
        } else {
            httpConnectTiming.setDnsTimeNanos(0L);
        }

        // the address *should* always be resolved at this point
        this.resolvedAddress = resolvedRemoteAddress.getAddress();
    }


    @Override
    public void proxyToServerResolutionFailed(String hostAndPort) {
        // since this is a CONNECT, which is not handled by the HarCaptureFilter, we need to create and populate the
        // entire HarEntry and add it to this har.
        HarEntry harEntry = createHarEntryForFailedCONNECT(HarCaptureUtil.getResolutionFailedErrorMessage(hostAndPort));
        har.getLog().addEntry(harEntry);

        // record the amount of time we attempted to resolve the hostname in the HarTimings object
        if (dnsResolutionStartedNanos > 0L) {
            harEntry.getTimings().setDns(System.nanoTime() - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        }

        httpConnectTimes.remove(clientAddress);
    }

    private HarEntry createHarEntryForFailedCONNECT(String errorMessage) {
        HarEntry harEntry = new HarEntry();
        harEntry.setStartedDateTime(requestStartTime);

        HarRequest request = createRequestForFailedConnect(originalRequest);
        harEntry.setRequest(request);

        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(errorMessage);

        populateTimingsForFailedCONNECT(harEntry);

        populateServerIpAddress(harEntry);


        return harEntry;
    }

    private HarRequest createRequestForFailedConnect(HttpRequest httpConnectRequest) {
        String url = getFullUrl(httpConnectRequest);

        return new HarRequest(httpConnectRequest.getMethod().toString(), url, httpConnectRequest.getProtocolVersion().text());
    }

    private void populateTimingsForFailedCONNECT(HarEntry harEntry) {
        HarTimings timings = harEntry.getTimings();

        if (connectionQueuedNanos > 0L && dnsResolutionStartedNanos > 0L) {
            timings.setBlocked(dnsResolutionStartedNanos - connectionQueuedNanos, TimeUnit.NANOSECONDS);
        }

        if (dnsResolutionStartedNanos > 0L && dnsResolutionFinishedNanos > 0L) {
            timings.setDns(dnsResolutionFinishedNanos - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        }

        if (connectionStartedNanos > 0L && connectionSucceededTimeNanos > 0L) {
            timings.setConnect(connectionSucceededTimeNanos - connectionStartedNanos, TimeUnit.NANOSECONDS);

            if (sslHandshakeStartedNanos > 0L) {
                timings.setSsl(connectionSucceededTimeNanos - this.sslHandshakeStartedNanos, TimeUnit.NANOSECONDS);
            }
        }

        if (sendStartedNanos > 0L && sendFinishedNanos >= 0L) {
            timings.setSend(sendFinishedNanos - sendStartedNanos, TimeUnit.NANOSECONDS);
        }

        if (sendFinishedNanos > 0L && responseReceiveStartedNanos >= 0L) {
            timings.setWait(responseReceiveStartedNanos - sendFinishedNanos, TimeUnit.NANOSECONDS);
        }

        // since this method is for HTTP CONNECT failures only, we can't populate a "received" time, since that would
        // require the CONNECT to be successful, in which case this method wouldn't be called.
    }

    private void populateServerIpAddress(HarEntry harEntry) {
        // populate the server IP address if it was resolved as part of this request. otherwise, populate the IP address from the cache.
        if (resolvedAddress != null) {
            harEntry.setServerIPAddress(resolvedAddress.getHostAddress());
        } else {
            String serverHost = HttpUtil.getHostFromRequest(modifiedHttpRequest);
            if (serverHost != null && !serverHost.isEmpty()) {
                String resolvedAddress = ResolvedHostnameCacheFilter.getPreviouslyResolvedAddressForHost(serverHost);
                if (resolvedAddress != null) {
                    harEntry.setServerIPAddress(resolvedAddress);
                } else {
                    // the resolvedAddress may be null if the ResolvedHostnameCacheFilter has expired the entry (which is unlikely),
                    // or in the far more common case that the proxy is using a chained proxy to connect to connect to the
                    // remote host. since the chained proxy handles IP address resolution, the IP address in the HAR must be blank.
                    Log.e(TAG, "Unable to find cached IP address for host: {}. IP address in HAR entry will be blank." + serverHost);
                }
            } else {
                Log.e(TAG, "Unable to identify host from request uri: {}" + modifiedHttpRequest.getUri());
            }
        }
    }

    public static HttpConnectTiming consumeConnectTimingForConnection(InetSocketAddress clientAddress) {
        return httpConnectTimes.remove(clientAddress);
    }

}
