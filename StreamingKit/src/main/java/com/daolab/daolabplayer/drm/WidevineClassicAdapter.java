

package com.daolab.daolabplayer.drm;

import android.content.Context;
import android.drm.DrmErrorEvent;
import android.drm.DrmEvent;
import android.drm.DrmInfoEvent;

import com.daolab.daolabplayer.LocalAssetsManager;
import com.daolab.daolabplayer.PKLog;

/**
 * Created by almond on almond
 */

class WidevineClassicAdapter extends DrmAdapter {

    private static final PKLog log = PKLog.get("WidevineClassicAdapter");

    private final Context context;

    WidevineClassicAdapter(Context context) {
        this.context = context;
    }

    @Override
    public boolean checkAssetStatus(String localAssetPath, String assetId, final LocalAssetsManager.AssetStatusListener listener) {
        WidevineClassicDrm widevineClassicDrm = new WidevineClassicDrm(context);
        WidevineClassicDrm.RightsInfo info = widevineClassicDrm.getRightsInfo(localAssetPath);
        if (listener != null) {
            listener.onStatus(localAssetPath, info.expiryTime, info.availableTime, true);
        }
        return true;
    }

    @Override
    public boolean registerAsset(final String localAssetPath, String assetId, String licenseUri, final LocalAssetsManager.AssetRegistrationListener listener) {
        WidevineClassicDrm widevineClassicDrm = new WidevineClassicDrm(context);
        widevineClassicDrm.setEventListener(new WidevineClassicDrm.EventListener() {
            @Override
            public void onError(DrmErrorEvent event) {
                log.d(event.toString());

                if (listener != null) {
                    listener.onFailed(localAssetPath, new Exception("License acquisition failed; DRM client error code: " + event.getType()));
                }
            }

            @Override
            public void onEvent(DrmEvent event) {
                log.d(event.toString());
                switch (event.getType()) {
                    case DrmInfoEvent.TYPE_RIGHTS_INSTALLED:
                        if (listener != null) {
                            listener.onRegistered(localAssetPath);
                        }
                        break;
                }
            }
        });
        widevineClassicDrm.acquireLocalAssetRights(localAssetPath, licenseUri);

        return true;
    }

    @Override
    public boolean refreshAsset(String localAssetPath, String assetId, String licenseUri, LocalAssetsManager.AssetRegistrationListener listener) {
        return registerAsset(localAssetPath, assetId, licenseUri, listener);
    }

    @Override
    public boolean unregisterAsset(final String localAssetPath, String assetId, final LocalAssetsManager.AssetRemovalListener listener) {
        WidevineClassicDrm widevineClassicDrm = new WidevineClassicDrm(context);
        widevineClassicDrm.setEventListener(new WidevineClassicDrm.EventListener() {
            @Override
            public void onError(DrmErrorEvent event) {
                log.d(event.toString());
            }

            @Override
            public void onEvent(DrmEvent event) {
                log.d(event.toString());
                switch (event.getType()) {
                    case DrmInfoEvent.TYPE_RIGHTS_REMOVED:
                        if (listener != null) {
                            listener.onRemoved(localAssetPath);
                        }
                        break;
                }
            }
        });
        widevineClassicDrm.removeRights(localAssetPath);
        return true;
    }
}
