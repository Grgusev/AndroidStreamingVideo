

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.daolabplayer.api.base.model.BasePlaybackContext;
import com.daolab.daolabplayer.api.phoenix.PhoenixErrorHelper;
import com.daolab.netkit.utils.ErrorElement;

import java.util.ArrayList;

/**
 * Created by tehilarozin on 02/11/2016.
 */

public class DaolabPlaybackContext extends BasePlaybackContext {

    private ArrayList<DaolabPlaybackSource> sources;


    public ArrayList<DaolabPlaybackSource> getSources() {
        return sources;
    }


    @Override
    protected ErrorElement getErrorElement(DaolabAccessControlMessage message) {
        switch (message.getCode()){
            case "OK":
                return null;
            default:
                return PhoenixErrorHelper.getErrorElement(message.getCode(), message.getMessage());
        }
    }
}
