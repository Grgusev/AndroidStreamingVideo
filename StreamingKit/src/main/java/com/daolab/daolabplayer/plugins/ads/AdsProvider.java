

package com.daolab.daolabplayer.plugins.ads;

import com.daolab.daolabplayer.ads.DLAdInfo;
import com.daolab.daolabplayer.ads.AdEnabledPlayerController;
import com.daolab.daolabplayer.plugins.ads.ima.IMAConfig;


public interface AdsProvider {
    IMAConfig getAdsConfig();

    void start();
    void destroyAdsManager();
    void resume();
    void pause();
    void contentCompleted();
    DLAdInfo getAdInfo();
    boolean isAdDisplayed();
    boolean isAdPaused();
    boolean isAdRequested();
    boolean isAllAdsCompleted();
    boolean isAdError();
    long getDuration();
    long getCurrentPosition();
    void setAdProviderListener(AdEnabledPlayerController adEnabledPlayerController);
    void removeAdProviderListener();
    void skipAd();
}
