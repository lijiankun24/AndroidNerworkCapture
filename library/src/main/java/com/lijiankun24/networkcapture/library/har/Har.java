package com.lijiankun24.networkcapture.library.har;

/**
 * Har.java
 * <p>
 * Created by lijiankun on 17/11/1.
 */

public class Har {

    private volatile HarLog log;

    public Har(HarLog log) {
        this.log = log;
    }

    public HarLog getLog() {
        return log;
    }

    public void setLog(HarLog log) {
        this.log = log;
    }
}
