package com.lijiankun24.networkcapture.library.proxy;

import org.littleshoot.proxy.HttpFiltersSource;

import java.net.InetAddress;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * BrowserMobProxyServer.java
 * <p>
 * Created by lijiankun on 17/10/30.
 */

public class BrowserMobProxyServer implements BrowserMobProxy{

    /**
     * The list of filterFactories that will generate the filters that implement browsermob-proxy behavior.
     */
    private final List<HttpFiltersSource> filterFactories = new CopyOnWriteArrayList<>();

    public List<HttpFiltersSource> getFilterFactories() {
        return filterFactories;
    }

    @Override
    public void start() {

    }

    @Override
    public void start(int port) {

    }

    @Override
    public void start(int port, InetAddress bindAddress) {

    }

    @Override
    public void start(int port, InetAddress clientBindAddress, InetAddress serverBindAddress) {

    }

    @Override
    public void stop() {

    }
}
