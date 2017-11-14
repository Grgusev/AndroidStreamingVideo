

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.netkit.connect.response.BaseResult;
import com.daolab.netkit.utils.ErrorElement;

/**
 * @hide
 */

public class DaolabLoginResponse extends BaseResult {

    private DaolabLoginSession loginSession;
    private DaolabOTTUser user;

    public DaolabLoginResponse(ErrorElement error) {
        super(error);
    }

    public DaolabLoginResponse() {
        super();
    }

    public DaolabLoginSession getLoginSession() {
        return loginSession;
    }

    public DaolabOTTUser getUser() {
        return user;
    }
}
