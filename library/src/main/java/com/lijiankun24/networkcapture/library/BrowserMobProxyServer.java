package com.lijiankun24.networkcapture.library;

import org.littleshoot.proxy.HttpFiltersSource;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BrowserMobProxyServer.java
 * <p>
 * Created by lijiankun on 17/10/30.
 */

public class BrowserMobProxyServer {

    /**
     * The list of filterFactories that will generate the filters that implement browsermob-proxy behavior.
     */
    private final List<HttpFiltersSource> filterFactories = new CopyOnWriteArrayList<>();

    public List<HttpFiltersSource> getFilterFactories() {
        return filterFactories;
    }
}
