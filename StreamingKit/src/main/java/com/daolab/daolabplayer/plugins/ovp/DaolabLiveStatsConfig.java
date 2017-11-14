

package com.daolab.daolabplayer.plugins.ovp;

import com.google.gson.JsonObject;

/**
 * Created by almond on 18/05/2017.
 */

public class DaolabLiveStatsConfig {


    public static final String PARTNER_ID = "partnerId";
    public static final String ENTRY_ID   = "entryId";
    public static final String BASE_URL   = "baseUrl";

    private int partnerId;
    private String entryId;
    private String baseUrl;

    public DaolabLiveStatsConfig(int partnerId, String entryId) {
        this.baseUrl = "https://stats.kaltura.com/api_v3/index.php";
        this.partnerId = partnerId;
        this.entryId = entryId;
    }

    public DaolabLiveStatsConfig(int partnerId, String entryId, String baseUrl) {
        this.baseUrl = baseUrl;
        this.partnerId = partnerId;
        this.entryId = entryId;
    }

    public DaolabLiveStatsConfig setPartnerId(int partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public DaolabLiveStatsConfig setEntryId(String entryId) {
        this.entryId = entryId;
        return this;
    }

    public DaolabLiveStatsConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public JsonObject toJSONObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PARTNER_ID, partnerId);
        jsonObject.addProperty(ENTRY_ID, entryId);
        jsonObject.addProperty(BASE_URL, baseUrl);

        return jsonObject;
    }
}
