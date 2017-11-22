package com.lijiankun24.networkcapture.library.har;

import com.lijiankun24.networkcapture.library.proxy.BrowserMobProxyServer;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * HarLog.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HarLog {

    private List<HarEntry> entries = new CopyOnWriteArrayList<>();

    public synchronized void addEntry(HarEntry entry) {
        entries.add(entry);
    }

    public List<HarEntry> getEntries() {
        return entries;
    }

    public void clearAllEntries() {
        entries.clear();
    }
}

