

package com.daolab.daolabplayer.api.phoenix;

import com.daolab.daolabplayer.PKMediaEntry;

/**
 * @hide
 */

public class APIDefines {

    public enum AssetReferenceType {
        Media("media"),
        InternalEpg("epg_internal"),
        ExternalEpg("epg_external");

        public String value;

        AssetReferenceType(String value){
            this.value = value;
        }
    }

    public enum LiveStreamType {
        Catchup("catchup"),
        StartOver("startOver"),
        TrickPlay("trickPlay");

        public String value;

        LiveStreamType(String value){
            this.value = value;
        }
    }


    public enum MediaType {
        Vod(DaolabAssetType.Media, PKMediaEntry.MediaEntryType.Vod),
        Channel(DaolabAssetType.Media, PKMediaEntry.MediaEntryType.Live),
        Recording(DaolabAssetType.Recording,PKMediaEntry.MediaEntryType.Vod),
        EPG(DaolabAssetType.Epg, PKMediaEntry.MediaEntryType.Live);

        private DaolabAssetType assetType;
        private PKMediaEntry.MediaEntryType mediaEntryType;

        MediaType(DaolabAssetType assetType, PKMediaEntry.MediaEntryType mediaEntryType){
            this.assetType = assetType;
            this.mediaEntryType = mediaEntryType;
        }

        public DaolabAssetType getAssetType() {
            return assetType;
        }

        public PKMediaEntry.MediaEntryType getMediaEntryType() {
            return mediaEntryType;
        }
    }


    public enum DaolabAssetType {
        Media("media"),
        Epg("epg"),
        Recording("recording");

        public String value;

        DaolabAssetType(String value){
            this.value = value;
        }
    }

    public enum PlaybackContextType {
        Trailer("TRAILER"),
        Catchup("CATCHUP"),
        StartOver("START_OVER"),
        Playback("PLAYBACK");

        public String value;

        PlaybackContextType(String value){
            this.value = value;
        }
    }

}

