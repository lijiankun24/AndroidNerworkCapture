package com.lijiankun24.networkcapture.library.filter;

import android.annotation.TargetApi;
import android.util.Log;

import com.lijiankun24.networkcapture.library.filter.support.HttpConnectTiming;
import com.lijiankun24.networkcapture.library.filter.util.HarCaptureUtil;
import com.lijiankun24.networkcapture.library.har.Har;
import com.lijiankun24.networkcapture.library.har.HarEntry;
import com.lijiankun24.networkcapture.library.har.HarNameValuePair;
import com.lijiankun24.networkcapture.library.har.HarRequest;
import com.lijiankun24.networkcapture.library.har.HarResponse;
import com.lijiankun24.networkcapture.library.util.BrowserMobHttpUtil;
import com.lijiankun24.networkcapture.library.util.L;

import java.net.InetAddress;
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

    private volatile boolean addressResolved = false;

    private volatile long dnsResolutionStartedNanos;

    private volatile long connectionQueuedNanos;

    private volatile long connectionStartedNanos;

    private volatile long sendStartedNanos;

    private volatile long sendFinishedNanos;

    private volatile long responseReceiveStartedNanos;

    private final Har har;

    private final HarEntry harEntry;

    private final InetSocketAddress clientAddress;

    private volatile HttpRequest capturedOriginalRequest;

    private ServerResponseCaptureFilter responseCaptureFilter;

    public HarCaptureFilter(HttpRequest originalRequest, ChannelHandlerContext ctx, Har har) {
        super(originalRequest, ctx);
        this.har = har;
        this.clientAddress = (InetSocketAddress) ctx.channel().remoteAddress();
        harEntry = new HarEntry("pageref");
    }

    @Override
    public HttpResponse clientToProxyRequest(HttpObject httpObject) {
        if (httpObject instanceof HttpRequest) {
            har.getLog().addEntry(harEntry);
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

    @Override
    public HttpObject serverToProxyResponse(HttpObject httpObject) {
        if (responseCaptureFilter != null) {
            responseCaptureFilter.serverToProxyResponse(httpObject);
        }

        if (httpObject instanceof HttpResponse) {
            HttpResponse httpResponse = (HttpResponse) httpObject;

            captureResponse(httpResponse);
        }

//        if (httpObject instanceof HttpContent) {
//            HttpContent httpContent = (HttpContent) httpObject;
//
//            captureResponseSize(httpContent);
//        }
//
//        if (httpObject instanceof LastHttpContent) {
//            if (dataToCapture.contains(CaptureType.RESPONSE_CONTENT)) {
//                captureResponseContent(responseCaptureFilter.getHttpResponse(), responseCaptureFilter.getFullResponseContents());
//            }
//
//            harEntry.getResponse().setBodySize(responseBodySize.get());
//        }
        return super.serverToProxyResponse(httpObject);
    }

    @Override
    public void serverToProxyResponseTimedOut() {
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(HarCaptureUtil.getResponseTimedOutErrorMessage());


        // include this timeout time in the HarTimings object
        long timeoutTimestampNanos = System.nanoTime();

        // if the proxy started to send the request but has not yet finished, we are currently "sending"
        if (sendStartedNanos > 0L && sendFinishedNanos == 0L) {
            harEntry.getTimings().setSend(timeoutTimestampNanos - sendStartedNanos, TimeUnit.NANOSECONDS);
        }
        // if the entire request was sent but the proxy has not begun receiving the response, we are currently "waiting"
        else if (sendFinishedNanos > 0L && responseReceiveStartedNanos == 0L) {
            harEntry.getTimings().setWait(timeoutTimestampNanos - sendFinishedNanos, TimeUnit.NANOSECONDS);
        }
        // if the proxy has already begun to receive the response, we are currenting "receiving"
        else if (responseReceiveStartedNanos > 0L) {
            harEntry.getTimings().setReceive(timeoutTimestampNanos - responseReceiveStartedNanos, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public InetSocketAddress proxyToServerResolutionStarted(String resolvingServerHostAndPort) {
        L.i("proxyToServerResolutionStarted");
        dnsResolutionStartedNanos = System.nanoTime();
        if (connectionQueuedNanos > 0L) {
            harEntry.getTimings().setBlocked(dnsResolutionStartedNanos - connectionQueuedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setBlocked(0L, TimeUnit.NANOSECONDS);
        }
        return super.proxyToServerResolutionStarted(resolvingServerHostAndPort);
    }

    @Override
    public void proxyToServerResolutionFailed(String hostAndPort) {
        L.i("proxyToServerResolutionFailed");
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);
        response.setError(HarCaptureUtil.getResolutionFailedErrorMessage(hostAndPort));

        if (dnsResolutionStartedNanos > 0L) {
            harEntry.getTimings().setDns(System.nanoTime() - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void proxyToServerResolutionSucceeded(String serverHostAndPort, InetSocketAddress resolvedRemoteAddress) {
        L.i("proxyToServerResolutionSucceeded");
        long dnsResolutionFinishedNanos = System.nanoTime();
        if (dnsResolutionStartedNanos > 0L) {
            harEntry.getTimings().setDns(dnsResolutionFinishedNanos - dnsResolutionStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setDns(0L, TimeUnit.NANOSECONDS);
        }
        InetAddress resolvedAddress = resolvedRemoteAddress.getAddress();
        if (resolvedAddress != null) {
            addressResolved = true;

            harEntry.setServerIPAddress(resolvedAddress.getHostAddress());
        }
    }

    @Override
    public void proxyToServerConnectionQueued() {
        L.i("proxyToServerConnectionQueued");
        this.connectionQueuedNanos = System.nanoTime();
    }

    @Override
    public void proxyToServerConnectionStarted() {
        L.i("proxyToServerConnectionStarted");
        this.connectionStartedNanos = System.nanoTime();
    }

    @Override
    public void proxyToServerConnectionFailed() {
        L.i("proxyToServerConnectionFailed");
        HarResponse response = HarCaptureUtil.createHarResponseForFailure();
        harEntry.setResponse(response);

        response.setError(HarCaptureUtil.getConnectionFailedErrorMessage());

        // record the amount of time we attempted to connect in the HarTimings object
        if (connectionStartedNanos > 0L) {
            harEntry.getTimings().setConnect(System.nanoTime() - connectionStartedNanos, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void proxyToServerConnectionSucceeded(ChannelHandlerContext serverCtx) {
        L.i("proxyToServerConnectionSucceeded");
        long connectionSucceededTimeNanos = System.nanoTime();

        // make sure the previous timestamp was captured, to avoid setting an absurd value in the har (see serverToProxyResponseReceiving())
        if (connectionStartedNanos > 0L) {
            harEntry.getTimings().setConnect(connectionSucceededTimeNanos - connectionStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setConnect(0L, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void proxyToServerRequestSending() {
        L.i("proxyToServerRequestSending");
        this.sendStartedNanos = System.nanoTime();

        if (!addressResolved) {
            populateAddressFromCache(capturedOriginalRequest);
        }
    }

    @Override
    public void proxyToServerRequestSent() {
        L.i("proxyToServerRequestSent");
        this.sendFinishedNanos = System.nanoTime();

        // make sure the previous timestamp was captured, to avoid setting an absurd value in the har (see serverToProxyResponseReceiving())
        if (sendStartedNanos > 0L) {
            harEntry.getTimings().setSend(sendFinishedNanos - sendStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setSend(0L, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void serverToProxyResponseReceiving() {
        L.i("serverToProxyResponseReceiving");
        this.responseReceiveStartedNanos = System.nanoTime();

        if (sendFinishedNanos > 0L && sendFinishedNanos < responseReceiveStartedNanos) {
            harEntry.getTimings().setWait(responseReceiveStartedNanos - sendFinishedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setWait(0L, TimeUnit.NANOSECONDS);
        }
    }

    @Override
    public void serverToProxyResponseReceived() {
        L.i("serverToProxyResponseReceived");
        long responseReceivedNanos = System.nanoTime();

        if (responseReceiveStartedNanos > 0L) {
            harEntry.getTimings().setReceive(responseReceivedNanos - responseReceiveStartedNanos, TimeUnit.NANOSECONDS);
        } else {
            harEntry.getTimings().setReceive(0L, TimeUnit.NANOSECONDS);
        }
    }

    protected void populateAddressFromCache(HttpRequest httpRequest) {
        String serverHost = getHost(httpRequest);

        if (serverHost != null && !serverHost.isEmpty()) {
            String resolvedAddress = ResolvedHostnameCacheFilter.getPreviouslyResolvedAddressForHost(serverHost);
            if (resolvedAddress != null) {
                harEntry.setServerIPAddress(resolvedAddress);
            } else {
                // the resolvedAddress may be null if the ResolvedHostnameCacheFilter has expired the entry (which is unlikely),
                // or in the far more common case that the proxy is using a chained proxy to connect to connect to the
                // remote host. since the chained proxy handles IP address resolution, the IP address in the HAR must be blank.
                Log.i("HarCaptureFilter", "Unable to find cached IP address for host: {}. IP address in HAR entry will be blank. " + serverHost);
            }
        } else {
            Log.i("HarCaptureFilter", "Unable to identify host from request uri: {}" + httpRequest.getUri());
        }
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


    protected void captureResponse(HttpResponse httpResponse) {
        HarResponse response = new HarResponse(httpResponse.getStatus().code(), httpResponse.getStatus().reasonPhrase(), httpResponse.getProtocolVersion().text());
        harEntry.setResponse(response);

        captureResponseHeaderSize(httpResponse);

        captureResponseMimeType(httpResponse);

    }

    protected void captureResponseHeaderSize(HttpResponse httpResponse) {
        String statusLine = httpResponse.getProtocolVersion().toString() + ' ' + httpResponse.getStatus().toString();
        // +2 => CRLF after status line, +4 => header/data separation
        long responseHeadersSize = statusLine.length() + 6;
        HttpHeaders headers = httpResponse.headers();
        responseHeadersSize += BrowserMobHttpUtil.getHeaderSize(headers);

        harEntry.getResponse().setHeadersSize(responseHeadersSize);
    }


    protected void captureResponseMimeType(HttpResponse httpResponse) {
        String contentType = HttpHeaders.getHeader(httpResponse, HttpHeaders.Names.CONTENT_TYPE);
        // don't set the mimeType to null, since mimeType is a required field
        if (contentType != null) {
            harEntry.getResponse().getContent().setMimeType(contentType);
        }
    }
}
