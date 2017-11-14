

package com.daolab.daolabplayer.addon.cast;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by almond on almond.
 */

public class OVPCastBuilder extends BasicCastBuilder<OVPCastBuilder> {


    public OVPCastBuilder setKs(@NonNull String ks) {
        castInfo.setKs(ks);
        return this;
    }


    @Override
    protected CastConfigHelper getCastHelper() {

        return new OVPCastConfigHelper();

    }


    @Override
    protected void validate(CastInfo castInfo) throws IllegalArgumentException {

        super.validate(castInfo);

        // ks isn't mandatory in OVP environment, but if you do set a ks it must be valid
        String ks = castInfo.getKs();
        if (ks != null && TextUtils.isEmpty(ks)) {
            throw new IllegalArgumentException();
        }


    }


}
