

package com.daolab.daolabplayer.plugins.ovp;

import com.daolab.daolabplayer.utils.Consts;
import com.google.gson.JsonObject;

/**
 * Created by almond on 18/05/2017.
 */

public class DaolabStatsConfig {


    public static final String PARTNER_ID = "partnerId";
    public static final String UICONF_ID  = "uiconfId";
    public static final String ENTRY_ID   = "entryId";
    public static final String BASE_URL   = "baseUrl";
    public static final String USER_ID    = "userId";
    public static final String CONTEXT_ID = "contextId";
    public static final String TIMER_INTERVAL = "timerInterval"; //in seconds

    private int partnerId;
    private int uiconfId;
    private String entryId;
    private String baseUrl;
    private String userId;
    private int contextId;
    private int timerInterval;

    public DaolabStatsConfig(int uiconfId, int partnerId, String entryId, String userId, int contextId) {
        this.timerInterval = Consts.DEFAULT_ANALYTICS_TIMER_INTERVAL_LOW_SEC;
        this.baseUrl = "https://stats.kaltura.com/api_v3/index.php";
        this.partnerId = partnerId;
        this.uiconfId = uiconfId;
        this.entryId = entryId;
        this.userId = userId;
        this.contextId = contextId;
    }

    public DaolabStatsConfig(int uiconfId, int partnerId, String entryId, String baseUrl, String userId, int contextId, int timerInterval) {
        this.timerInterval = timerInterval;
        this.baseUrl = baseUrl;
        this.partnerId = partnerId;
        this.uiconfId = uiconfId;
        this.entryId = entryId;
        this.userId = userId;
        this.contextId = contextId;
    }

    public DaolabStatsConfig setPartnerId(int partnerId) {
        this.partnerId = partnerId;
        return this;
    }

    public DaolabStatsConfig setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public DaolabStatsConfig setContextId(int contextId) {
        this.contextId = contextId;
        return this;
    }

    public DaolabStatsConfig setUiconfId(int uiconfId) {
        this.uiconfId = uiconfId;
        return this;
    }

    public DaolabStatsConfig setEntryId(String entryId) {
        this.entryId = entryId;
        return this;
    }

    public DaolabStatsConfig setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public DaolabStatsConfig setTimerInterval(int timerInterval) {
        this.timerInterval = timerInterval;
        return this;
    }

    public int getPartnerId() {
        return partnerId;
    }

    public int getUiconfId() {
        return uiconfId;
    }

    public String getEntryId() {
        return entryId;
    }

    public String getBaseUrl() {
        return baseUrl;
    }

    public int getTimerInterval() {
        return timerInterval;
    }

    public String getUserId() {
        return userId;
    }

    public int getContextId() {
        return contextId;
    }

    public JsonObject toJSONObject() {
        JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty(PARTNER_ID, partnerId);
        jsonObject.addProperty(UICONF_ID, uiconfId);
        jsonObject.addProperty(ENTRY_ID, entryId);
        jsonObject.addProperty(BASE_URL, baseUrl);
        jsonObject.addProperty(USER_ID, userId);
        jsonObject.addProperty(CONTEXT_ID, contextId);
        jsonObject.addProperty(TIMER_INTERVAL, timerInterval);

        return jsonObject;
    }
}
