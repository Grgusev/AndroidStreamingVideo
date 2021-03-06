

package com.daolab.daolabplayer;

import android.content.Context;
import android.support.annotation.Nullable;

import com.daolab.daolabplayer.player.MediaSupport;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by almond @ Daolab on 13/10/2016.
 */
public class PlayKitManager {

    public static final String VERSION_STRING = BuildConfig.VERSION_NAME;
    public static final String CLIENT_TAG = "playkit/android-" + VERSION_STRING;

    private static boolean shouldSendDeviceCapabilitiesReport = false;
    

    static {
        PKLog.i("PlayKitManager", "PlayKit " + VERSION_STRING);
    }
    
    
    private static Map<String, PKPlugin.Factory> sPluginFactories = new HashMap<>();

    public static void registerPlugins(Context context, PKPlugin.Factory... pluginFactories) {
        Context applicationContext = context != null ? context.getApplicationContext() : null;
        for (PKPlugin.Factory factory : pluginFactories) {
            String name = factory.getName();
            if (applicationContext != null && sPluginFactories.put(name, factory) == null) {
                factory.warmUp(applicationContext);
            }
        }
    }
    
    static PKPlugin createPlugin(String name) {
        PKPlugin.Factory pluginFactory = sPluginFactories.get(name);
        return pluginFactory == null ? null : pluginFactory.newInstance();
    }

    public static Player loadPlayer(Context context, @Nullable PKPluginConfigs pluginConfigs) {
        
        MediaSupport.initialize(context);
        
        if (shouldSendDeviceCapabilitiesReport) {
            PKDeviceCapabilities.maybeSendReport(context);
        }
        
        PlayerLoader playerLoader = new PlayerLoader(context);
        playerLoader.load(pluginConfigs != null ? pluginConfigs : new PKPluginConfigs());
        return playerLoader;
    }
    

    public static void disableDeviceCapabilitiesReport() {
        shouldSendDeviceCapabilitiesReport = false;
    }
}

