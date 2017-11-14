

package com.daolab.daolabplayer.api.phoenix;

import com.daolab.netkit.connect.request.RequestBuilder;
import com.daolab.netkit.connect.request.RequestElement;
import com.daolab.daolabplayer.api.phoenix.services.PhoenixService;

/**
 */

public class PhoenixRequestBuilder extends RequestBuilder<PhoenixRequestBuilder> {

    @Override
    public RequestElement build() {
        addParams(PhoenixService.getPhoenixConfigParams());
        return super.build();
    }
}
