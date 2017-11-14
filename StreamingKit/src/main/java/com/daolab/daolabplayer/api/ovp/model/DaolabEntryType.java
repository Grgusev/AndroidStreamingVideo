

package com.daolab.daolabplayer.api.ovp.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by tehilarozin on 21/12/2016.
 */

public enum DaolabEntryType {

    @SerializedName("-1")
    AUTOMATIC("-1"),
    @SerializedName("1")
    MEDIA_CLIP("1"),
    @SerializedName("7")
    LIVE_STREAM("7"),
    @SerializedName("5")
    PLAYLIST("5");

    public String type;

    DaolabEntryType(String type) {
        this.type = type;
    }
}
