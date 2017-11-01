package com.lijiankun24.networkcapture.library.har;

/**
 * Har.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class Har {

    private HarLog mHarLog = null;

    public Har() {
    }

    public Har(HarLog harLog) {
        mHarLog = harLog;
    }

    public HarLog getHarLog() {
        return mHarLog;
    }

    public void setHarLog(HarLog harLog) {
        mHarLog = harLog;
    }
}
