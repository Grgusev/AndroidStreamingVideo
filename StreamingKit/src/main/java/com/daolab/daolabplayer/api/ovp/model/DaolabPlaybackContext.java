

package com.daolab.daolabplayer.api.ovp.model;

import com.daolab.netkit.utils.ErrorElement;
import com.daolab.daolabplayer.api.base.model.BasePlaybackContext;
import com.daolab.daolabplayer.api.ovp.DaolabOvpErrorHelper;

import java.util.ArrayList;

/**
 * @hide
 */

public class DaolabPlaybackContext extends BasePlaybackContext{

    private ArrayList<DaolabPlaybackSource> sources;
    private ArrayList<DaolabFlavorAsset> flavorAssets;

    public DaolabPlaybackContext() {
    }

    public ArrayList<DaolabPlaybackSource> getSources() {
        return sources;
    }

    public ArrayList<DaolabFlavorAsset> getFlavorAssets() {
        return flavorAssets;
    }

    @Override
    protected ErrorElement getErrorElement(DaolabAccessControlMessage message) {
        return DaolabOvpErrorHelper.getErrorElement(message.getCode(), message.getMessage());
    }
}

