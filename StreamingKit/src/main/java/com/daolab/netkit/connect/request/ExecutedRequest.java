package com.daolab.netkit.connect.request;

/**
 * @hide
 */
public class ExecutedRequest implements com.daolab.netkit.connect.response.ResponseElement {

    String requestId;
    int code = -1;
    String response = "";
    boolean isSuccess = false;
    com.daolab.netkit.utils.ErrorElement error = null;

    public ExecutedRequest requestId(String id) {
        this.requestId = id;
        return this;
    }

    public ExecutedRequest code(int code) {
        this.code = code;
        return this;
    }

    public ExecutedRequest response(String response) {
        this.response = response;
        return this;
    }

    public ExecutedRequest success(boolean success) {
        this.isSuccess = success;
        return this;
    }

    public ExecutedRequest error(com.daolab.netkit.utils.ErrorElement error) {
        this.error = error;
        return this;
    }

    public ExecutedRequest error(Exception exception) {
        this.error = com.daolab.netkit.utils.ErrorElement.fromException(exception);
        return this;
    }

    @Override
    public String getCode() {
        return code+"";
    }

    @Override
    public String getResponse() {
        return response;
    }

    @Override
    public com.daolab.netkit.utils.ErrorElement getError() {
        return error;
    }

    @Override
    public boolean isSuccess() {
        return isSuccess;
    }

    @Override
    public String getRequestId() {
        return requestId;
    }

}

