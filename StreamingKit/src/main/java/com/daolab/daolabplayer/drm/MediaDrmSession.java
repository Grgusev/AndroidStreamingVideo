

package com.daolab.daolabplayer.drm;

import android.annotation.TargetApi;
import android.media.DeniedByServerException;
import android.media.MediaDrm;
import android.media.MediaDrmException;
import android.media.NotProvisionedException;
import android.os.Build;
import android.support.annotation.NonNull;

import com.google.android.exoplayer2.drm.FrameworkMediaDrm;

import java.util.Map;

/**
 * Created by almond on almond
 */

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
class MediaDrmSession {

    private final FrameworkMediaDrm mMediaDrm;
    private byte[] mSessionId;

    private MediaDrmSession(@NonNull FrameworkMediaDrm mediaDrm) {
        mMediaDrm = mediaDrm;
    }

    static MediaDrmSession open(@NonNull FrameworkMediaDrm mediaDrm) throws MediaDrmException {
        MediaDrmSession session = new MediaDrmSession(mediaDrm);
        session.mSessionId = mediaDrm.openSession();
        return session;
    }

    byte[] getId() {
        return mSessionId;
    }

    void close() {
        mMediaDrm.closeSession(mSessionId);
    }

    void restoreKeys(byte[] keySetId) {
        mMediaDrm.restoreKeys(mSessionId, keySetId);
    }


     Map<String, String> queryKeyStatus() {
        return mMediaDrm.queryKeyStatus(mSessionId);
    }

    FrameworkMediaDrm.KeyRequest getOfflineKeyRequest(byte[] initData, String mimeType) {
        try {
            return mMediaDrm.getKeyRequest(mSessionId, initData, mimeType, MediaDrm.KEY_TYPE_OFFLINE, null);
        } catch (NotProvisionedException e) {
            throw new WidevineNotSupportedException(e);
        }
    }

    byte[] provideKeyResponse(byte[] keyResponse) throws DeniedByServerException {
        try {
            return mMediaDrm.provideKeyResponse(mSessionId, keyResponse);
        } catch (NotProvisionedException e) {
            throw new WidevineNotSupportedException(e);
        }
    }
}
