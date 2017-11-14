package com.daolab.netkit.connect.response;

/**
 * Created by tehilarozin on 06/09/2016.
 */
public interface ResultElement<T> {

    T getResponse();

    boolean isSuccess();

    com.daolab.netkit.utils.ErrorElement getError();

}
