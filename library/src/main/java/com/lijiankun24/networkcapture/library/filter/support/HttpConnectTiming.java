package com.lijiankun24.networkcapture.library.filter.support;

public class HttpConnectTiming {
    private volatile long blockedTimeNanos = -1;
    private volatile long dnsTimeNanos = -1;
    private volatile long connectTimeNanos = -1;
    private volatile long sslHandshakeTimeNanos = -1;

    public void setConnectTimeNanos(long connectTimeNanos) {
        this.connectTimeNanos = connectTimeNanos;
    }

    public void setSslHandshakeTimeNanos(long sslHandshakeTimeNanos) {
        this.sslHandshakeTimeNanos = sslHandshakeTimeNanos;
    }

    public void setBlockedTimeNanos(long blockedTimeNanos) {
        this.blockedTimeNanos = blockedTimeNanos;
    }

    public void setDnsTimeNanos(long dnsTimeNanos) {
        this.dnsTimeNanos = dnsTimeNanos;
    }

    public long getConnectTimeNanos() {
        return connectTimeNanos;
    }

    public long getSslHandshakeTimeNanos() {
        return sslHandshakeTimeNanos;
    }

    public long getBlockedTimeNanos() {
        return blockedTimeNanos;
    }

    public long getDnsTimeNanos() {
        return dnsTimeNanos;
    }
}
