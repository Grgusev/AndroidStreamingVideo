package com.daolab.daolabui;

import android.app.Activity;

/**
 * Created by almond on 8/5/2017.
 */

public interface DaolabPlayer {

    public void setActivity(Activity activity);

    public void toggleFullscreen();

    public void startPlayEntry();

    public void startPlay();

    public void prepare();

    public void pausePlay();

    public void resumePlay();

    public void setHLSURL(String url, String id, String name);

    public void onPause();

    public void onResume();

    public void setAutoPlayResume(boolean play);

    public void setInline(boolean inline);

    public void setFullScreen(boolean fullScreen);

    public String getStatus();

    public boolean getInline();

    public long getPosition();

    public void setScreenOrientation(int orientation);

    public void setVolume(float volume);

    public float getVolume();

    public void setMute();

    public Boolean isMute();

    public void setThumbnailUrl(String url);

    public void setThumbnailResource(int resourceId);

    public void showThumbnail(boolean visible);

    public void setTrackInfoShow(boolean visible);

    public void setGoogleAdLink(String tagUrl);

    public void setVideoPlayerEventListener(DaolabVideoPlayerEventListener listener);
}
