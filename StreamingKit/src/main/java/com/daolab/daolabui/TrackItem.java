package com.daolab.daolabui;

/**
 * Created by almond on 30/11/2016.
 */
public class TrackItem {

    private String trackName;
    private String uniqueId;

    public TrackItem(String trackName, String uniqueId) {
        this.trackName = trackName;
        this.uniqueId = uniqueId;
    }

    public String getTrackName() {
        return trackName;
    }

    public String getUniqueId() {
        return uniqueId;
    }
}
