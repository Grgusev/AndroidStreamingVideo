

package com.daolab.daolabplayer.api.ovp.model;

import com.daolab.netkit.connect.response.BaseResult;

/**
 * @hide
 */

public class DaolabSessionInfo extends BaseResult {

    String sessionType;
    long expiry;
    String userId;


    public long getExpiry() {
        return expiry;
    }

    public String getUserId() {
        return userId;
    }
}
