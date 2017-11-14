

package com.daolab.daolabplayer.addon.cast;

import android.support.annotation.NonNull;
import android.text.TextUtils;

/**
 * Created by almond on almond.
 */

public class TVPAPICastBuilder extends BasicCastBuilder<TVPAPICastBuilder> {


    public TVPAPICastBuilder setFormat(@NonNull String format) {
        castInfo.setFormat(format);
        return this;
    }


    public TVPAPICastBuilder setInitObject(@NonNull String initObject) {
        castInfo.setInitObject(initObject);
        return this;
    }


    @Override
    protected CastConfigHelper getCastHelper() {

        return new TVPAPICastConfigHelper();

    }


    @Override
    protected void validate(CastInfo castInfo) throws IllegalArgumentException {

        super.validate(castInfo);

        if (TextUtils.isEmpty(castInfo.getFormat())) {
            throw new IllegalArgumentException();
        }

        if (TextUtils.isEmpty(castInfo.getInitObject())) {
            throw new IllegalArgumentException();
        }
    }


}
