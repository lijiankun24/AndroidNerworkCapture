package com.lijiankun24.networkcapture.library;

import com.lijiankun24.networkcapture.library.har.HarLog;
import com.lijiankun24.networkcapture.library.proxy.BrowserMobProxyServer;

/**
 * NetworkProxyManager.java
 * <p>
 * Created by lijiankun on 17/10/30.
 */

public class NetworkProxyManager {

    private static NetworkProxyManager INSTANCE = null;

    private BrowserMobProxyServer mServer = null;

    private NetworkProxyManager() {
    }

    public static NetworkProxyManager getInstance() {
        if (INSTANCE == null) {
            synchronized (NetworkProxyManager.class) {
                if (INSTANCE == null) {
                    INSTANCE = new NetworkProxyManager();
                }
            }
        }
        return INSTANCE;
    }


    public void startProxy(String proxyHost, int proxyPort) {
        System.setProperty("http.proxyHost", proxyHost);
        System.setProperty("http.proxyPort", String.valueOf(proxyPort));
        System.setProperty("https.proxyHost", proxyHost);
        System.setProperty("https.proxyPort", String.valueOf(proxyPort));
        mServer = new BrowserMobProxyServer();
        mServer.start(proxyPort);
    }

    public HarLog getHarLog() {
        if (mServer == null) {
            return null;
        }
        return mServer.getHar().getLog();
    }
}
