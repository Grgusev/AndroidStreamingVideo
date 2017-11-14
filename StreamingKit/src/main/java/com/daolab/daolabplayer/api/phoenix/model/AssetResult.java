

package com.daolab.daolabplayer.api.phoenix.model;

import com.google.gson.annotations.SerializedName;
import com.daolab.netkit.connect.response.BaseResult;

/**
 * @hide
 */

public class AssetResult extends BaseResult {

    @SerializedName(value = "result")
    public DaolabMediaAsset asset;

}
