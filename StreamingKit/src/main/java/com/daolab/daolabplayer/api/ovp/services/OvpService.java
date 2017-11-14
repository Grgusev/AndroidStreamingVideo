

package com.daolab.daolabplayer.api.ovp.services;

import com.daolab.daolabplayer.PlayKitManager;
import com.daolab.daolabplayer.api.ovp.OvpConfigs;
import com.google.gson.JsonObject;
import com.daolab.netkit.connect.request.MultiRequestBuilder;

/**
 * @hide
 */
public class OvpService {

    public static String[] getRequestConfigKeys(){
        return new String[]{"clientTag", "apiVersion", "format"};
    }

    public static JsonObject getOvpConfigParams(){
        JsonObject params = new JsonObject();
        params.addProperty("clientTag", PlayKitManager.CLIENT_TAG);
        params.addProperty("apiVersion", OvpConfigs.ApiVersion);
        params.addProperty("format",1); //json format

        return params;
    }

    public static MultiRequestBuilder getMultirequest(String baseUrl, String ks){
        return getMultirequest(baseUrl, ks, -1);
    }

    public static MultiRequestBuilder getMultirequest(String baseUrl, String ks, int partnerId){
        JsonObject ovpParams = OvpService.getOvpConfigParams();
        ovpParams.addProperty("ks", ks);
        if(partnerId > 0) {
            ovpParams.addProperty("partnerId", partnerId);
        }
        return (MultiRequestBuilder) new MultiRequestBuilder().method("POST")
                .url(baseUrl)
                .params(ovpParams)
                .service("multirequest");
    }
}
