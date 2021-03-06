

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.netkit.connect.response.BaseResult;

/**
 * @hide
 */

public class DaolabMediaFile extends BaseResult {

    long duration;
    int id;
    int assetId;
    String url;
    String type;

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getAssetId() {
        return assetId;
    }

    public void setAssetId(int assetId) {
        this.assetId = assetId;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }
}
