

package com.daolab.daolabplayer.addon.cast;

import android.text.TextUtils;

import org.json.JSONException;
import org.json.JSONObject;


class OVPCastConfigHelper extends CastConfigHelper {


    @Override
    protected String getSessionInfo(CastInfo castInfo) {

        return castInfo.getKs();

    }



    @Override
    protected void setProxyData(JSONObject flashVars, String ks, String fileFormat,
                                String entryId) {


        JSONObject proxyData = new JSONObject();


        //it's possible to work in OVP environment without ks
        if (!TextUtils.isEmpty(ks)) {

            try {

                proxyData.put("ks", ks);
                flashVars.put("proxyData", proxyData);

            } catch(JSONException e) {
               log.e(e.getMessage());
            }

        }


    }


}
