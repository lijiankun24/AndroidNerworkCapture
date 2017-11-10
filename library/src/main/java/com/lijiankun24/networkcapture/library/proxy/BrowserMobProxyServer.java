package com.lijiankun24.networkcapture.library.proxy;

import android.util.Log;

import com.lijiankun24.networkcapture.library.filter.BrowserMobHttpFilterChain;
import com.lijiankun24.networkcapture.library.filter.HarCaptureFilter;
import com.lijiankun24.networkcapture.library.filter.HttpConnectHarCaptureFilter;
import com.lijiankun24.networkcapture.library.har.Har;
import com.lijiankun24.networkcapture.library.har.HarLog;
import com.lijiankun24.networkcapture.library.har.HarNameVersion;
import com.lijiankun24.networkcapture.library.har.HarPage;
import com.lijiankun24.networkcapture.library.util.L;

import org.littleshoot.proxy.HttpFilters;
import org.littleshoot.proxy.HttpFiltersSource;
import org.littleshoot.proxy.HttpFiltersSourceAdapter;
import org.littleshoot.proxy.HttpProxyServer;
import org.littleshoot.proxy.HttpProxyServerBootstrap;
import org.littleshoot.proxy.impl.DefaultHttpProxyServer;
import org.littleshoot.proxy.impl.ProxyUtils;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpRequest;

/**
 * BrowserMobProxyServer.java
 * <p>
 * Created by lijiankun on 17/10/30.
 */

public class BrowserMobProxyServer implements BrowserMobProxy {

    private final AtomicBoolean harCaptureFilterEnabled = new AtomicBoolean(true);

    private final AtomicInteger harPageCount = new AtomicInteger(0);

    private final List<HttpFiltersSource> filterFactories = new CopyOnWriteArrayList<>();

    public List<HttpFiltersSource> getFilterFactories() {
        return filterFactories;
    }

    private static final HarNameVersion HAR_CREATOR_VERSION = new HarNameVersion("BrowserMob Proxy", BrowserMobProxyUtil.getVersionString());

    private volatile HarPage currentHarPage;

    private volatile Har har;

    private volatile HttpProxyServer proxyServer;

    @Override
    public void start() {

    }

    @Override
    public void start(int port) {
        this.start(port, null, null);
    }

    @Override
    public void start(int port, InetAddress bindAddress) {

    }

    @Override
    public void start(int port, InetAddress clientBindAddress, InetAddress serverBindAddress) {
        HttpProxyServerBootstrap bootstrap = DefaultHttpProxyServer.bootstrap()
                .withPort(port)
                .withFiltersSource(new HttpFiltersSourceAdapter() {

                    @Override
                    public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                        Log.i("lijk", "filterRequest 2 ");
                        return new BrowserMobHttpFilterChain(originalRequest, ctx, BrowserMobProxyServer.this);
                    }
                });
        proxyServer = bootstrap.start();
        String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                .format(new Date(System.currentTimeMillis()));
        this.newHar(time);
    }

    @Override
    public void stop() {

    }

    @Override
    public Har newHar() {
        return newHar(null);
    }

    @Override
    public Har newHar(String initialPageRef) {
        return newHar(initialPageRef, null);
    }

    @Override
    public Har newHar(String initialPageRef, String initialPageTitle) {
        this.har = new Har(new HarLog(HAR_CREATOR_VERSION, this));

        addHarCaptureFilter();

        harPageCount.set(0);


        newPage(initialPageRef, initialPageTitle);

        return this.har;
    }

    @Override
    public Har getHar() {
        return har;
    }

    @Override
    public Har newPage() {
        return newPage(null);
    }

    @Override
    public Har newPage(String pageRef) {
        return newPage(pageRef, null);
    }

    @Override
    public Har newPage(String pageRef, String pageTitle) {
        if (har == null) {
            throw new IllegalStateException("No HAR exists for this proxy. Use newHar() to create a new HAR before calling newPage().");
        }

        Har endOfPageHar = null;

        if (currentHarPage != null) {
            String currentPageRef = currentHarPage.getId();

            // end the previous page, so that page-wide timings are populated
            endPage();

            // the interface requires newPage() to return the Har as it was immediately after the previous page was ended.
            endOfPageHar = BrowserMobProxyUtil.copyHarThroughPageRef(har, currentPageRef);
        }

        if (pageRef == null) {
            pageRef = "Page " + harPageCount.getAndIncrement();
        }

        if (pageTitle == null) {
            pageTitle = pageRef;
        }

        HarPage newPage = new HarPage(pageRef, pageTitle);
        har.getLog().addPage(newPage);

        currentHarPage = newPage;

        return endOfPageHar;
    }

    @Override
    public Har endHar() {
        return null;
    }

    public void endPage() {
        if (har == null) {
            throw new IllegalStateException("No HAR exists for this proxy. Use newHar() to create a new HAR.");
        }

        HarPage previousPage = this.currentHarPage;
        this.currentHarPage = null;

        if (previousPage == null) {
            return;
        }

        previousPage.getPageTimings().setOnLoad(new Date().getTime() - previousPage.getStartedDateTime().getTime());
    }

    public HarPage getCurrentHarPage() {
        return currentHarPage;
    }

    protected void addHarCaptureFilter() {
        // the HAR capture filter is (relatively) expensive, so only enable it when a HAR is being captured. furthermore,
        // restricting the HAR capture filter to requests where the HAR exists, as well as  excluding HTTP CONNECTs
        // from the HAR capture filter, greatly simplifies the filter code.
        addHttpFilterFactory(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                Har har = getHar();
                L.i("" + (har != null && !ProxyUtils.isCONNECT(originalRequest)));
                if (har != null && !ProxyUtils.isCONNECT(originalRequest)) {
                    return new HarCaptureFilter(originalRequest, ctx, har);
                } else {
                    return null;
                }
            }
        });

        // HTTP CONNECTs are a special case, since they require special timing and error handling
        addHttpFilterFactory(new HttpFiltersSourceAdapter() {
            @Override
            public HttpFilters filterRequest(HttpRequest originalRequest, ChannelHandlerContext ctx) {
                Har har = getHar();
                if (har != null && ProxyUtils.isCONNECT(originalRequest)) {
                    return new HttpConnectHarCaptureFilter(originalRequest, ctx, har, getCurrentHarPage() == null ? null : getCurrentHarPage().getId());
                } else {
                    return null;
                }
            }
        });
    }

    public void addHttpFilterFactory(HttpFiltersSource filterFactory) {
        filterFactories.add(filterFactory);
        L.i("size is " + filterFactories.size());
    }
}
