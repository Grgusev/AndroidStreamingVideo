package com.daolab.netkit.connect.request;

import com.daolab.netkit.connect.executor.APIOkRequestsExecutor;

import java.util.UUID;

/**
 * @hide
 */
public class RequestIdFactory implements APIOkRequestsExecutor.IdFactory {
    @Override
    public String factorId(String factor) {
        return UUID.randomUUID().toString() + "::" + (factor!=null ? factor : System.currentTimeMillis());
    }
}
