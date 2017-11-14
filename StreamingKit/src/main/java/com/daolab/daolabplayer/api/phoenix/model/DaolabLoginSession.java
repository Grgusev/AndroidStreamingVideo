

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.netkit.connect.response.BaseResult;

/**
 * @hide
 */

public class DaolabLoginSession extends BaseResult {
    String refreshToken;
    String ks;

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getKs() {
        return ks;
    }
}
