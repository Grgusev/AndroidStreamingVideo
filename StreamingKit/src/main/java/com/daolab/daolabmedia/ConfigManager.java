package com.daolab.daolabmedia;

import com.android.volley.*;
import com.android.volley.toolbox.*;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Iterator;
import java.util.Map;
import java.util.HashMap;

/**
 * Created by carlos on 14/8/2017.
 */

public class ConfigManager {
    public interface mediaCallbackInterface {
        void onLoadedVideoID(JSONObject result);
    }

    static String mediaServerURL = "https://vod.cdn.hk01.com";
    static String identifier = "";

    static public void findVideoWithVideoID(android.content.Context context, String videoID, final mediaCallbackInterface callback) {
        RequestQueue queue = Volley.newRequestQueue(context);

        String url = mediaServerURL + "/" + videoID + "/info.json";

        StringRequest stringRequest = new StringRequest(Request.Method.GET, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        android.util.Log.i("Config Manager", "Response is: " + response);
                        try {
                            JSONObject jsonResult = new JSONObject(response);

                            if(jsonResult.has("url_s3")) {
                                String urlS3 = jsonResult.getString("url_s3");
                                if(urlS3.length() > 0) {
                                    jsonResult.put("url", mediaServerURL + "/" + urlS3);
                                }
                                jsonResult.remove("url_s3");
                            }

                            if(jsonResult.has("img_s3")) {
                                String imgS3 = jsonResult.getString("img_s3");
                                if(imgS3.length() > 0) {
                                    jsonResult.put("img", mediaServerURL + "/" + imgS3);
                                }
                                jsonResult.remove("img_s3");
                            }

                            if(jsonResult.has("sub_s3")) {
                                JSONObject subS3 = jsonResult.getJSONObject("sub_s3");
                                Iterator keys = subS3.keys();
                                if(keys.hasNext()) {
                                    JSONObject sub = new JSONObject();
                                    while (keys.hasNext()) {
                                        String key = (String) keys.next();
                                        sub.put(key, mediaServerURL + "/" + subS3.getString(key));
                                    }
                                    jsonResult.put("sub", sub);
                                }
                                jsonResult.remove("sub_s3");
                            }

                            android.util.Log.i("Config Manager", "S3 JSON is: " + jsonResult);

                            callback.onLoadedVideoID(jsonResult);
                        } catch(JSONException je) {
                            android.util.Log.e("Config Manager", "Invalid JSON");
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                android.util.Log.e("Config Manager", "No result");
            }
        }) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Cookie", "DaolabPass=" + getPassString());
                return headers;
            }
        };

        queue.add(stringRequest);
    }

    static public void setIdentifier(String id) {
        identifier = id;
    }

    static public String getPassString() {
        return "daolab-android-" + identifier + "-" + System.currentTimeMillis();
    }
}
