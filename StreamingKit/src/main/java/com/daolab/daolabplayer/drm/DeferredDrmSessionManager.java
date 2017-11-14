

package com.daolab.daolabplayer.drm;

import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmInitData;
import com.google.android.exoplayer2.drm.DrmSession;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.drm.HttpMediaDrmCallback;
import com.google.android.exoplayer2.drm.UnsupportedDrmException;
import com.google.android.exoplayer2.extractor.mp4.PsshAtomUtil;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;
import com.daolab.daolabplayer.LocalAssetsManager;
import com.daolab.daolabplayer.PKDrmParams;
import com.daolab.daolabplayer.PKError;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.player.MediaSupport;
import com.daolab.daolabplayer.player.PKPlayerErrorType;

import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.daolab.daolabplayer.Utils.toBase64;

/**
 * @hide
 */

public class DeferredDrmSessionManager implements DrmSessionManager<FrameworkMediaCrypto>, DefaultDrmSessionManager.EventListener {

    private static final PKLog log = PKLog.get("DeferredDrmSessionManager");

    private Handler mainHandler;
    private DrmSessionListener drmSessionListener;
    private HttpDataSource.Factory dataSourceFactory;
    private LocalAssetsManager.LocalMediaSource localMediaSource = null;
    private DefaultDrmSessionManager<FrameworkMediaCrypto> drmSessionManager = null;

    public interface DrmSessionListener {
        void onError(PKError error);
    }

    public DeferredDrmSessionManager(Handler mainHandler, HttpDataSource.Factory factory, DrmSessionListener drmSessionListener) {
        this.mainHandler = mainHandler;
        this.dataSourceFactory = factory;
        this.drmSessionListener = drmSessionListener;
    }

    public void setMediaSource(PKMediaSource mediaSource) {
        if (Util.SDK_INT < 18) {
            drmSessionManager = null;
            return;
        }

        try {
            String licenseUrl = getLicenseUrl(mediaSource);
            drmSessionManager = DefaultDrmSessionManager.newWidevineInstance(new HttpMediaDrmCallback(licenseUrl, dataSourceFactory), null, mainHandler, this);
            if (mediaSource instanceof LocalAssetsManager.LocalMediaSource) {
                localMediaSource = (LocalAssetsManager.LocalMediaSource) mediaSource;
            }
        } catch (UnsupportedDrmException exception) {
            PKError error = new PKError(PKPlayerErrorType.DRM_ERROR, "This device doesn't support widevine modular", exception);
            drmSessionListener.onError(error);
        }
    }

    @Override
    public DrmSession<FrameworkMediaCrypto> acquireSession(Looper playbackLooper, DrmInitData drmInitData) {
        if (drmSessionManager == null) {
            return null;
        }

        if (localMediaSource != null) {
            byte[] offlineKey;
            DrmInitData.SchemeData schemeData = getWidevineInitData(drmInitData);
            try {
                if (schemeData != null) {
                    offlineKey = localMediaSource.getStorage().load(toBase64(schemeData.data));
                    drmSessionManager.setMode(DefaultDrmSessionManager.MODE_PLAYBACK, offlineKey);
                    localMediaSource = null;
                }
            } catch (FileNotFoundException e) {
                PKError error = new PKError(PKPlayerErrorType.DRM_ERROR, "Failed to obtain offline licence from LocalDataStore. Requested key: " + Arrays.toString(schemeData.data) + ", for keysetId not found.", e);
                drmSessionListener.onError(error);
            }
        }

        return new SessionWrapper(playbackLooper, drmInitData, drmSessionManager);
    }

    @Override
    public void releaseSession(DrmSession drmSession) {
        if (drmSession instanceof SessionWrapper) {
            ((SessionWrapper) drmSession).release();
        } else {
            throw new IllegalStateException("Can't release unknown session");
        }
    }

    private DrmInitData.SchemeData getWidevineInitData(DrmInitData drmInitData) {
        if (drmInitData == null) {
            log.e("No PSSH in media");
            return null;
        }


        DrmInitData.SchemeData schemeData = drmInitData.get(MediaSupport.WIDEVINE_UUID);
        if (schemeData == null) {
            return null;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            // Prior to L the Widevine CDM required data to be extracted from the PSSH atom.
            byte[] psshData = PsshAtomUtil.parseSchemeSpecificData(schemeData.data, MediaSupport.WIDEVINE_UUID);
            if (psshData == null) {
                log.w("Extraction failed. schemeData isn't a Widevine PSSH atom, so leave it unchanged.");
            } else {
                schemeData = new DrmInitData.SchemeData(MediaSupport.WIDEVINE_UUID, schemeData.mimeType, psshData);
            }
        }
        return schemeData;
    }

    private String getLicenseUrl(PKMediaSource mediaSource) {
        String licenseUrl = null;

        if (mediaSource.hasDrmParams()) {
            List<PKDrmParams> drmData = mediaSource.getDrmData();
            for (PKDrmParams pkDrmParam : drmData) {
                // selecting WidevineCENC as default right now
                if (PKDrmParams.Scheme.WidevineCENC == pkDrmParam.getScheme()) {
                    licenseUrl = pkDrmParam.getLicenseUri();
                    break;
                }
            }
        }
        return licenseUrl;
    }

    @Override
    public void onDrmKeysLoaded() {
        log.d("onDrmKeysLoaded");
    }

    @Override
    public void onDrmSessionManagerError(Exception e) {
        log.d("onDrmSessionManagerError");
        PKError error = new PKError(PKPlayerErrorType.DRM_ERROR, e.getMessage(), e);
        drmSessionListener.onError(error);
    }

    @Override
    public void onDrmKeysRestored() {
        log.d("onDrmKeysRestored");
    }

    @Override
    public void onDrmKeysRemoved() {
        log.d("onDrmKeysRemoved");
    }

}


class SessionWrapper implements DrmSession<FrameworkMediaCrypto> {

    private DrmSession<FrameworkMediaCrypto> realDrmSession;
    private DrmSessionManager<FrameworkMediaCrypto> realDrmSessionManager;

    SessionWrapper(Looper playbackLooper, DrmInitData drmInitData, DrmSessionManager<FrameworkMediaCrypto> drmSessionManager) {
        this.realDrmSession = drmSessionManager.acquireSession(playbackLooper, drmInitData);
        this.realDrmSessionManager = drmSessionManager;
    }

    void release() {
        realDrmSessionManager.releaseSession(realDrmSession);
        realDrmSessionManager = null;
        realDrmSession = null;
    }

    @Override
    public int getState() {
        return realDrmSession.getState();
    }

    @Override
    public FrameworkMediaCrypto getMediaCrypto() {
        return realDrmSession.getMediaCrypto();
    }

    @Override
    public boolean requiresSecureDecoderComponent(String mimeType) {
        return realDrmSession.requiresSecureDecoderComponent(mimeType);
    }

    @Override
    public DrmSessionException getError() {
        return realDrmSession.getError();
    }


    @Override
    public Map<String, String> queryKeyStatus() {
        return realDrmSession.queryKeyStatus();
    }

    @Override
    public byte[] getOfflineLicenseKeySetId() {
        return realDrmSession.getOfflineLicenseKeySetId();
    }
}
