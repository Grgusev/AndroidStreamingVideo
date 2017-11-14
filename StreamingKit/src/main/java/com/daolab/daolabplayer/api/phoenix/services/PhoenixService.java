

package com.daolab.daolabplayer.api.phoenix.services;

import android.support.annotation.Nullable;
import android.text.TextUtils;

import com.daolab.daolabplayer.api.phoenix.PhoenixConfigs;
import com.google.gson.JsonObject;
import com.daolab.netkit.connect.request.MultiRequestBuilder;

/**
 * @hide
 */
public class PhoenixService {

    public static JsonObject getPhoenixConfigParams(){
        JsonObject params = new JsonObject();
        params.addProperty("clientTag", PhoenixConfigs.ClientTag);
        params.addProperty("apiVersion",PhoenixConfigs.ApiVersion);

        return params;
    }

    public static MultiRequestBuilder getMultirequest(String baseUrl, @Nullable String ks){
        JsonObject params = getPhoenixConfigParams();
        if(!TextUtils.isEmpty(ks)) {
            params.addProperty("ks", ks);
        }
        return (MultiRequestBuilder) new MultiRequestBuilder().service("multirequest").method("POST").url(baseUrl).params(params);
    }
}
