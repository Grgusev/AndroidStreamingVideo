package com.daolab.netkit.connect.executor;


import com.daolab.netkit.connect.request.RequestElement;
import com.daolab.netkit.connect.response.ResponseElement; /**
 */
public interface RequestQueue {

    void setDefaultConfiguration(com.daolab.netkit.connect.request.RequestConfiguration config);

    String queue(RequestElement request);

    ResponseElement execute(RequestElement request);

    void cancelRequest(String reqId);

    //boolean hasRequest(String reqId);

    void clearRequests();

    boolean isEmpty();

    void enableLogs(boolean enable);
}
