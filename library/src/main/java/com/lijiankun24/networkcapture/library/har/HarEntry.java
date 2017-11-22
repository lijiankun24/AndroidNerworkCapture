package com.lijiankun24.networkcapture.library.har;

import java.util.Date;
import java.util.concurrent.TimeUnit;

public class HarEntry {
    private volatile Date startedDateTime;
    private volatile HarRequest request;
    private volatile HarResponse response;
    private volatile HarTimings timings = new HarTimings();
    private volatile String serverIPAddress;
    private volatile String connection;
    private volatile String comment = "";

    public HarEntry() {
    }

    public Date getStartedDateTime() {
        return startedDateTime;
    }

    public void setStartedDateTime(Date startedDateTime) {
        this.startedDateTime = startedDateTime;
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

    public HarRequest getRequest() {
        return request;
    }

    public void setRequest(HarRequest request) {
        this.request = request;
    }

    public HarResponse getResponse() {
        return response;
    }

    public void setResponse(HarResponse response) {
        this.response = response;
    }

    public HarTimings getTimings() {
        return timings;
    }

    public void setTimings(HarTimings timings) {
        this.timings = timings;
    }

    public String getServerIPAddress() {
        return serverIPAddress;
    }

    public void setServerIPAddress(String serverIPAddress) {
        this.serverIPAddress = serverIPAddress;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getConnection() {
        return connection;
    }

    public void setConnection(String connection) {
        this.connection = connection;
    }
}
