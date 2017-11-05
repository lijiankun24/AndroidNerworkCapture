package com.lijiankun24.networkcapture.library.proxy;

import com.lijiankun24.networkcapture.library.har.Har;

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

    Har newHar();

    Har newHar(String initialPageRef);

    Har newHar(String initialPageRef, String initialPageTitle);

    Har getHar();

    Har newPage();

    Har newPage(String pageRef);

    Har newPage(String pageRef, String pageTitle);

    Har endHar();
}
