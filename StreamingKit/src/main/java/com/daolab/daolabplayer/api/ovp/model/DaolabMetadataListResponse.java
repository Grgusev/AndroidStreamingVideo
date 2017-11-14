

package com.daolab.daolabplayer.api.ovp.model;

import com.daolab.netkit.connect.response.BaseResult;

import java.util.List;
/**
 * @hide
 */

public class DaolabMetadataListResponse extends BaseResult {

    public List<DaolabMetadata> objects;
    int totalCount;
}
