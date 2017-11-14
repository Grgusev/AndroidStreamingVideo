

package com.daolab.daolabplayer.plugins.playback;

import android.net.Uri;

import com.daolab.daolabplayer.PKRequestParams;
import com.daolab.daolabplayer.Player;

import static com.daolab.daolabplayer.PlayKitManager.CLIENT_TAG;
import static com.daolab.daolabplayer.Utils.toBase64;

/**
 * Created by almond @ Daolab on 28/03/2017.
 */
public class DaolabPlaybackRequestAdapter implements PKRequestParams.Adapter {

    private final String applicationName;
    private String playSessionId;
    
    public static void install(Player player, String applicationName) {
        DaolabPlaybackRequestAdapter decorator = new DaolabPlaybackRequestAdapter(applicationName, player);
        player.getSettings().setContentRequestAdapter(decorator);
    }

    private DaolabPlaybackRequestAdapter(String applicationName, Player player) {
        this.applicationName = applicationName;
        updateParams(player);
    }
    
    @Override
    public PKRequestParams adapt(PKRequestParams requestParams) {
        Uri url = requestParams.url;

        if (url.getPath().contains("/playManifest/")) {
            Uri alt = url.buildUpon()
                    .appendQueryParameter("clientTag", CLIENT_TAG)
                    .appendQueryParameter("referrer", toBase64(applicationName.getBytes()))
                    .appendQueryParameter("playSessionId", playSessionId)
                    .build();

            String lastPathSegment = requestParams.url.getLastPathSegment();
            if (lastPathSegment.endsWith(".wvm")) {
                // in old android device it will not play wvc if url is not ended in wvm
                alt = alt.buildUpon().appendQueryParameter("name", lastPathSegment).build();
            }
            return new PKRequestParams(alt, requestParams.headers);
        }

        return requestParams;
    }

    @Override
    public void updateParams(Player player) {
        this.playSessionId = player.getSessionId();
    }
}
