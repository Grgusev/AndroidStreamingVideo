

package com.daolab.daolabplayer.api.ovp.model;

import com.daolab.netkit.connect.response.BaseResult;

import java.util.ArrayList;

/**
 * @hide
 */

public class DaolabEntryContextDataResult extends BaseResult {

    ArrayList<DaolabFlavorAsset> flavorAssets;


    public ArrayList<DaolabFlavorAsset> getFlavorAssets() {
        return flavorAssets;
    }


}
