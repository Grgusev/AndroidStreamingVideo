

package com.daolab.daolabplayer.api.ovp;

import com.daolab.netkit.connect.request.RequestBuilder;
import com.daolab.netkit.connect.request.RequestElement;
import com.daolab.daolabplayer.api.ovp.services.OvpService;

/**
 * @hide
 */

public class OvpRequestBuilder extends RequestBuilder<OvpRequestBuilder> {

    @Override
    public RequestElement build() {
        addParams(OvpService.getOvpConfigParams());
        return super.build();
    }
}
