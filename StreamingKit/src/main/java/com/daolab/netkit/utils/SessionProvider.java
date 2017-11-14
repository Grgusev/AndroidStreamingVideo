package com.daolab.netkit.utils;

/**
 * Created by tehilarozin on 13/11/2016.
 */

/**
 * provides session related configuration data to be used by who ever needs to communicate with
 * a remote data provider
 */
public interface SessionProvider {

    String baseUrl();

    void getSessionToken(OnCompletion<com.daolab.netkit.connect.response.PrimitiveResult> completion);

    int partnerId();

}
