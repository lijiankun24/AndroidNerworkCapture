package com.lijiankun24.networkcapture.library.proxy;

import java.net.InetAddress;

/**
 * BrowserMobProxy.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public interface BrowserMobProxy {

    void start();

    void start(int port);

    void start(int port, InetAddress bindAddress);

    void start(int port, InetAddress clientBindAddress, InetAddress serverBindAddress);

    void stop();
}
