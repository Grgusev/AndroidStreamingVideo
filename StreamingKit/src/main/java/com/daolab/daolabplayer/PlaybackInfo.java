

package com.daolab.daolabplayer;

/**
 * Data object that holds information about currently playing media.
 * Created by almond on almond.
 */

public class PlaybackInfo {

    private String mediaUrl;
    private long videoBitrate;
    private long audioBitrate;
    private long videoThroughput;
    private long videoWidth;
    private long videoHeight;
    private boolean isLiveStream;


    public PlaybackInfo(String mediaUrl, long videoBitrate, long audioBitrate, long videoThroughput, long videoWidth, long videoHeight, boolean isLiveStream) {
        this.mediaUrl = mediaUrl;
        this.videoBitrate = videoBitrate;
        this.audioBitrate = audioBitrate;
        this.videoThroughput = videoThroughput;
        this.videoWidth   = videoWidth;
        this.videoHeight  = videoHeight;
        this.isLiveStream = isLiveStream;
    }

    /**
     * @return - the current playing media url.
     */
    public String getMediaUrl() {
        return mediaUrl;
    }

    /**
     *
     * @return - the current playing video track bitrate.
     */
    public long getVideoBitrate() {
        return videoBitrate;
    }

    /**
     *
     * @return - the current playing audio track bitrate.
     */
    public long getAudioBitrate() {
        return audioBitrate;
    }

    /**
     *
     * @return - the current playing video throughput.
     */
    public long getVideoThroughput() {
        return videoThroughput;
    }

    /**
     *
     * @return - the current playing video width.
     */
    public long getVideoWidth() {
        return videoWidth;
    }

    /**
     *
     * @return - the current playing video height.
     */
    public long getVideoHeight() {
        return videoHeight;
    }

    /**
     *
     * @return - the type of stream live or not.
     */
    public boolean getIsLiveStream() {
        return isLiveStream;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("mediaUrl =").append(mediaUrl).append(System.getProperty("line.separator"));
        sb.append("videoBitrate =").append(videoBitrate).append(System.getProperty("line.separator"));
        sb.append("audioBitrate =").append(audioBitrate).append(System.getProperty("line.separator"));
        sb.append("videoThroughput =").append(videoThroughput).append(System.getProperty("line.separator"));
        sb.append("videoWidth =").append(videoWidth).append(System.getProperty("line.separator"));
        sb.append("videoHeight =").append(videoHeight).append(System.getProperty("line.separator"));
        sb.append("isLiveStream =").append(isLiveStream).append(System.getProperty("line.separator"));
        return sb.toString();
    }
}
