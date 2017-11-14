

package com.daolab.daolabplayer.mediaproviders.base;

import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.api.phoenix.APIDefines;

/**
 * Created by tehilarozin on 02/04/2017.
 */

public enum MediaType {
    Vod(APIDefines.DaolabAssetType.Media, PKMediaEntry.MediaEntryType.Vod),
    Channel(APIDefines.DaolabAssetType.Media, PKMediaEntry.MediaEntryType.Live),
    Recording(APIDefines.DaolabAssetType.Recording,PKMediaEntry.MediaEntryType.Vod),
    EPG(APIDefines.DaolabAssetType.Epg, PKMediaEntry.MediaEntryType.Live);

    MediaType(APIDefines.DaolabAssetType assetType, PKMediaEntry.MediaEntryType mediaEntryType){
        this.assetType = assetType;
        this.mediaEntryType = mediaEntryType;
    }

    private APIDefines.DaolabAssetType assetType;
    private PKMediaEntry.MediaEntryType mediaEntryType;

    public APIDefines.DaolabAssetType getAssetType() {
        return assetType;
    }

    public PKMediaEntry.MediaEntryType getMediaEntryType() {
        return mediaEntryType;
    }
}