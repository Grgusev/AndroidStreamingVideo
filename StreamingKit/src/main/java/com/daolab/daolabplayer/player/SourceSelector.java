

package com.daolab.daolabplayer.player;

import android.support.annotation.Nullable;

import com.daolab.daolabplayer.LocalAssetsManager;
import com.daolab.daolabplayer.PKDrmParams;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PKMediaFormat;
import com.daolab.daolabplayer.PKMediaSource;

import java.util.List;

/**
 * Created by almond @ Daolab on 29/11/2016.
 */

class SourceSelector {
    
    private static final PKLog log = PKLog.get("SourceSelector");
    private final PKMediaEntry mediaEntry;
    
    SourceSelector(PKMediaEntry mediaEntry) {
        this.mediaEntry = mediaEntry;
    }
    
    @Nullable
    private PKMediaSource sourceByFormat(PKMediaFormat format) {
        for (PKMediaSource source : mediaEntry.getSources()) {
            if (source.getMediaFormat() == format) {
                return source;
            }
        }
        return null;
    }
    
    @Nullable
    PKMediaSource getPreferredSource() {

        // If PKMediaSource is local, there is no need to look for the preferred source,
        // because it is only one.
        PKMediaSource localMediaSource = getLocalSource();
        if(localMediaSource != null){
            return localMediaSource;
        }

        // Default preference: DASH, HLS, WVM, MP4, MP3

        PKMediaFormat[] pref = {PKMediaFormat.dash, PKMediaFormat.hls, PKMediaFormat.wvm, PKMediaFormat.mp4, PKMediaFormat.mp3};
        
        for (PKMediaFormat format : pref) {
            PKMediaSource source = sourceByFormat(format);
            if (source == null) {
                continue;
            }

            List<PKDrmParams> drmParams = source.getDrmData();
            if (drmParams != null && !drmParams.isEmpty()) {
                for (PKDrmParams params : drmParams) {
                    if (params.isSchemeSupported()) {
                        return source;
                    }
                }
                // This source doesn't have supported params
                continue;
            }
            return source;
        }
        return null;
    }

    static PKMediaSource selectSource(PKMediaEntry mediaEntry) {
        return new SourceSelector(mediaEntry).getPreferredSource();
    }

    private PKMediaSource getLocalSource(){
        for (PKMediaSource source : mediaEntry.getSources()) {
            if (source instanceof LocalAssetsManager.LocalMediaSource) {
                return source;
            }
        }
        return null;
    }
}

