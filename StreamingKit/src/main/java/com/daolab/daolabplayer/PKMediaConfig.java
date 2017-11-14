

package com.daolab.daolabplayer;

/**
 * Created by almond @ Daolab on 22/02/2017.
 */
public class PKMediaConfig {
    private long startPosition = 0;
    private PKMediaEntry mediaEntry;

    /**
     * Getter for start position. Default is 0.
     * Note, that start position is in seconds.
     *
     * @return - the start position
     */
    public long getStartPosition() {
        return startPosition;
    }

    /**
     * Setter for start position.
     * Note, that start position is in seconds.
     *
     * @param startPosition - the position from which the media should start.
     * @return - the config object.
     */
    public PKMediaConfig setStartPosition(long startPosition) {
        this.startPosition = startPosition;
        return this;
    }

    public PKMediaEntry getMediaEntry() {
        return mediaEntry;
    }

    public PKMediaConfig setMediaEntry(PKMediaEntry mediaEntry) {
        this.mediaEntry = mediaEntry;
        return this;
    }
}
