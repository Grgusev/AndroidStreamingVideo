package com.daolab.netkit.utils;

/**
 * @hide
 */

public class Accessories {

    public static <D> com.daolab.netkit.connect.response.ResultElement<D> buildResult(final D data, final ErrorElement error) {
        return new com.daolab.netkit.connect.response.ResultElement<D>() {
            @Override
            public D getResponse() {
                return data;
            }

            @Override
            public boolean isSuccess() {
                return null == error;
            }

            @Override
            public ErrorElement getError() {
                return error;
            }
        };
    }

    public static com.daolab.netkit.connect.response.ResponseElement buildResponse(final String data, final ErrorElement error) {

        //return (ResponseElement) buildResult(data, error);

        return new com.daolab.netkit.connect.response.ResponseElement() {
            @Override
            public String getCode() {
                return error == null ? com.daolab.netkit.connect.response.ResponseElement.Ok : error.getCode();
            }

            @Override
            public String getRequestId() {
                return null;
            }

            @Override
            public String getResponse() {
                return data;
            }

            @Override
            public boolean isSuccess() {
                return error==null;
            }

            @Override
            public ErrorElement getError() {
                return error;
            }
        };
    }
}
