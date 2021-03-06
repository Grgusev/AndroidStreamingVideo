

package com.daolab.daolabplayer.api.phoenix.services;

import android.text.TextUtils;

import com.google.gson.JsonObject;
import com.daolab.daolabplayer.api.phoenix.APIDefines;
import com.daolab.daolabplayer.api.phoenix.PhoenixRequestBuilder;

import java.util.List;

/**
 * @hide
 */

public class AssetService extends PhoenixService {

    public static PhoenixRequestBuilder get(String baseUrl, String ks, String assetId, APIDefines.AssetReferenceType referenceType) {
        JsonObject params = new JsonObject();
        params.addProperty("ks", ks);
        params.addProperty("id", assetId);
        params.addProperty("assetReferenceType", referenceType.value);
        params.addProperty("type", referenceType.value); // sometimes request expect type as property sometimes assetReferenceType
        // needed to make sure response will retrieve the media file no matter if apiVersion property supplied or not
        params.addProperty("with","[{\"type\": \"files\"}]");

        return new PhoenixRequestBuilder()
                .service("asset")
                .action("get")
                .method("POST")
                .url(baseUrl)
                .tag("asset-get")
                .params(params);
    }

    /**
     * builds the request for detailed asset sources data, including DRM data if has any.
     * @param baseUrl - api base server url
     * @param ks - valid session token
     * @param assetId - Asset id
     * @param assetType - {@link APIDefines.DaolabAssetType}
     * @param contextOptions - list of extra details to narrow search of sources
     * @return
     */
    public static PhoenixRequestBuilder getPlaybackContext(String baseUrl, String ks, String assetId,
                                                           APIDefines.DaolabAssetType assetType, DaolabPlaybackContextOptions contextOptions){

        JsonObject params = new JsonObject();
        params.addProperty("ks", ks);
        params.addProperty("assetId", assetId);
        params.addProperty("assetType", assetType.value);
        params.add("contextDataParams", contextOptions != null ? contextOptions.toJson() : new JsonObject());

        return new PhoenixRequestBuilder()
                .service("asset")
                .action("getPlaybackContext")
                .method("POST")
                .url(baseUrl)
                .tag("asset-getPlaybackContext")
                .params(params);
    }


    public static class DaolabPlaybackContextOptions {

        private APIDefines.PlaybackContextType context;
        private String protocol;
        private String assetFileIds;
        private String referrer;

        public DaolabPlaybackContextOptions(APIDefines.PlaybackContextType context){
            this.context = context;
        }

        public DaolabPlaybackContextOptions setMediaProtocol(String protocol){
            this.protocol = protocol;
            return this;
        }

        public DaolabPlaybackContextOptions setReferrer(String referrer){
            this.referrer = referrer;
            return this;
        }

        public DaolabPlaybackContextOptions setMediaFileIds(String ids){
            this.assetFileIds = ids;
            return this;
        }

        public DaolabPlaybackContextOptions setMediaFileIds(List<String> ids){
            this.assetFileIds = TextUtils.join(",", ids);
            return this;
        }

        public JsonObject toJson(){
            JsonObject params = new JsonObject();
            if(context != null) {
                params.addProperty("context", context.value);
            }

            if(!TextUtils.isEmpty(protocol)) {
                params.addProperty("mediaProtocol", protocol);
            }

            if(!TextUtils.isEmpty(assetFileIds)) {
                params.addProperty("assetFileIds", assetFileIds);
            }

            if(!TextUtils.isEmpty(referrer)) {
                params.addProperty("referrer", referrer);
            }

            return params;
        }
    }

}
