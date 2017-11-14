

package com.daolab.daolabplayer.api.ovp.model;

import com.daolab.netkit.connect.response.BaseResult;

import java.util.List;

/**
 * @hide
 */

public class DaolabBaseEntryListResponse extends BaseResult {

    public List<DaolabMediaEntry> objects;
    int totalCount;
}
