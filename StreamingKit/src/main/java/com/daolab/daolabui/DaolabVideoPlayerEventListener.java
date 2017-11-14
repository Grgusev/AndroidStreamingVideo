package com.daolab.daolabui;

import java.util.Map;

/**
 * Created by almond on 8/21/2017.
 */

public interface DaolabVideoPlayerEventListener {
    public void onPlaybackChange(String name, Map<String, Object> attributes);

    public void onScreenChange(String name, Map<String, Object> attributes);

    public void onAudioChange(String name, Map<String, Object> attributes);

    public void onSubtitleChange(String name, Map<String, Object> mParams);

    public void onQualityChange(String name, Map<String, Object> mParams);
}
