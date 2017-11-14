package com.daolab.netkit.connect.request;

/**
 */

public interface RequestConfiguration {

    long getReadTimeout();

    long getWriteTimeout();

    long getConnectTimeout();

    int getRetry();
}
