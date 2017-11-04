package com.lijiankun24.networkcapture.library.har;

import com.lijiankun24.networkcapture.library.proxy.BrowserMobProxyServer;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * HarLog.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class HarLog {

    private List<HarEntry> entries = new CopyOnWriteArrayList<>();

    private volatile HarNameVersion creator = new HarNameVersion("BrowserMob Proxy", "BrowserMob Version");

    private BrowserMobProxyServer server;

    public HarLog() {
    }

    public HarLog(HarNameVersion creator,BrowserMobProxyServer server) {
        this.creator = creator;
        this.server = server;
    }

    public synchronized void addEntry(HarEntry entry) {
        int count = 0;
        for (HarEntry har : entries) {
            if (entry.getPageref().equals(har.getPageref())) {
                count++;
            }
        }
        if (count >= 999) {
            if (server != null) {
                String time = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.CHINA)
                        .format(new Date(System.currentTimeMillis()));

//                // 检查是否存在重复添加
//                Boolean repeatAdd = false;
//                for (HarPage page : pages) {
//                    if (page.getId().equals(time)) {
//                        repeatAdd = true;
//                    }
//                }
//                if (!repeatAdd) {
//                    server.newPage(time);
//                }
            }
        }
        entries.add(entry);
    }

    public void clearAllEntries() {
        entries.clear();
    }
}
