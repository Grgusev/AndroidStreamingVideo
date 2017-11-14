

package com.daolab.daolabplayer.ads;

import com.daolab.daolabplayer.plugins.ads.AdPositionType;

public interface DLAdInfo {

    String   getAdDescription();
    String   getAdId();
    String   getAdSystem();
    boolean  isAdSkippable();
    String   getAdTitle();
    String   getAdContentType();
    int      getAdWidth();
    int      getAdHeight();
    int      getTotalAdsInPod();
    int      getAdIndexInPod();
    int      getPodCount();
    int      getPodIndex();
    boolean  isBumper();
    long     getAdPodTimeOffset();
    long     getAdDuration();
    long     getAdPlayHead();
    AdPositionType getAdPositionType();
}
