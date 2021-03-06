

package com.daolab.daolabplayer.player;

import android.net.Uri;

import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.PKRequestParams;

/**
 * Created by almond @ Daolab on 29/03/2017.
 */
class PKMediaSourceConfig {
    PKMediaSource mediaSource;
    PKRequestParams.Adapter adapter;
    boolean cea608CaptionsEnabled;
    boolean useTextureView;

    PKMediaSourceConfig(PKMediaSource mediaSource, PKRequestParams.Adapter adapter, boolean cea608CaptionsEnabled, boolean useTextureView) {
        this.mediaSource = mediaSource;
        this.adapter = adapter;
        this.useTextureView = useTextureView;
        this.cea608CaptionsEnabled = cea608CaptionsEnabled;
    }

    Uri getUrl() {
        Uri uri = Uri.parse(mediaSource.getUrl());
        if (adapter == null) {
            return uri;
        } else {
            return adapter.adapt(new PKRequestParams(uri, null)).url;
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        PKMediaSourceConfig that = (PKMediaSourceConfig) o;

        if (mediaSource != null ? !mediaSource.equals(that.mediaSource) : that.mediaSource != null) {
            return false;
        }
        return adapter != null ? adapter.equals(that.adapter) : that.adapter == null;
    }

    @Override
    public int hashCode() {
        int result = mediaSource != null ? mediaSource.hashCode() : 0;
        result = 31 * result + (adapter != null ? adapter.hashCode() : 0);
        return result;
    }
}
