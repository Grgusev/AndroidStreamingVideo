

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.daolabplayer.api.base.model.BasePlaybackSource;

/**
 * Created by tehilarozin on 13/02/2017.
 */

public class DaolabPlaybackSource extends BasePlaybackSource {
    private int assetId;
    private int id;
    private String type; //Device types as defined in the system (MediaFileFormat)
    private long duration;
    private String externalId;

    public int getId() {
        return id;
    }

    public long getDuration() {
        return duration;
    }

    public String getType() {
        return type;
    }

}
