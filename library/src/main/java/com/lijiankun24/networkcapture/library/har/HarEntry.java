package com.lijiankun24.networkcapture.library.har;

import java.util.concurrent.TimeUnit;

/**
 * HarEntry.java
 * <p>
 * Created by lijiankun on 17/11/4.
 */

public class HarEntry {

    private volatile String pageref;

    private volatile HarRequest request;

    private volatile HarResponse response;

    private HarTimings mHarTimings = new HarTimings();

    public HarEntry(String pageref) {
        this.pageref = pageref;
    }

    public String getPageref() {
        return pageref;
    }

    public void setPageref(String pageref) {
        this.pageref = pageref;
    }

    public HarRequest getRequest() {
        return request;
    }

    public void setRequest(HarRequest request) {
        this.request = request;
    }


    public void setResponse(HarResponse response) {
        this.response = response;
    }

    public HarTimings getTimings() {
        return mHarTimings;
    }

    public long getTime() {
        return getTime(TimeUnit.MILLISECONDS);
    }

    public long getTime(TimeUnit timeUnit) {
        HarTimings timings = getTimings();
        if (timings == null) {
            return -1;
        }

        long timeNanos = 0;
        if (timings.getBlocked(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getBlocked(TimeUnit.NANOSECONDS);
        }

        if (timings.getDns(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getDns(TimeUnit.NANOSECONDS);
        }

        if (timings.getConnect(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getConnect(TimeUnit.NANOSECONDS);
        }

        if (timings.getSend(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getSend(TimeUnit.NANOSECONDS);
        }

        if (timings.getWait(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getWait(TimeUnit.NANOSECONDS);
        }

        if (timings.getReceive(TimeUnit.NANOSECONDS) > 0) {
            timeNanos += timings.getReceive(TimeUnit.NANOSECONDS);
        }

        return timeUnit.convert(timeNanos, TimeUnit.NANOSECONDS);
    }
}
