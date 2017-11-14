

package com.daolab.daolabplayer.plugins;

import android.content.Context;

import com.google.gson.JsonObject;
import com.daolab.daolabplayer.MessageBus;
import com.daolab.daolabplayer.PKEvent;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaConfig;
import com.daolab.daolabplayer.PKPlugin;
import com.daolab.daolabplayer.Player;
import com.daolab.daolabplayer.PlayerDecorator;

/**
 * @hide
 */

public class SamplePlugin extends PKPlugin {

    private static final String TAG = "SamplePlugin";
    private static final PKLog log = PKLog.get("SamplePlugin");

    private Player player;
    private Context context;
    private int delay;

    public static final Factory factory = new Factory() {
        @Override
        public String getName() {
            return "Sample";
        }

        @Override
        public PKPlugin newInstance() {
            return new SamplePlugin();
        }

        @Override
        public void warmUp(Context context) {
            
        }
    };

    @Override
    protected void onLoad(Player player, Object config, final MessageBus messageBus, Context context) {
        log.i("Loading");
        this.player = player;
        this.context = context;
        delay = ((JsonObject) config).getAsJsonPrimitive("delay").getAsInt();
        log.v("delay=" + delay);
        
        messageBus.listen(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                log.d("onEvent: " + event);
            }
        });
    }

    @Override
    protected void onUpdateMedia(PKMediaConfig mediaConfig) {
        
    }

    @Override
    protected void onUpdateConfig(Object config) {
        
    }

    @Override
    protected void onApplicationPaused() {

    }

    @Override
    protected void onApplicationResumed() {

    }

    @Override
    public void onDestroy() {

    }

    @Override
    protected PlayerDecorator getPlayerDecorator() {
        return new PlayerDecorator() {
            @Override
            public void play() {
                super.play();
            }
        };
    }
}
