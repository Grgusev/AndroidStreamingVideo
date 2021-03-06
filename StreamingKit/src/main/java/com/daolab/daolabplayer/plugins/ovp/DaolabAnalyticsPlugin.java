

package com.daolab.daolabplayer.plugins.ovp;

import android.content.Context;

import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PlayerEvent;
import com.daolab.daolabplayer.utils.Consts;
import com.google.gson.JsonObject;
import com.daolab.netkit.connect.executor.APIOkRequestsExecutor;
import com.daolab.netkit.connect.executor.RequestQueue;
import com.daolab.netkit.connect.request.RequestBuilder;
import com.daolab.netkit.connect.response.ResponseElement;
import com.daolab.netkit.utils.OnRequestCompletion;
import com.daolab.daolabplayer.LogEvent;
import com.daolab.daolabplayer.MessageBus;
import com.daolab.daolabplayer.PKEvent;
import com.daolab.daolabplayer.PKMediaConfig;
import com.daolab.daolabplayer.PKPlugin;
import com.daolab.daolabplayer.PlayKitManager;
import com.daolab.daolabplayer.Player;
import com.daolab.daolabplayer.Utils;
import com.daolab.daolabplayer.api.ovp.services.AnalyticsService;

import java.util.TimerTask;

/**
 * Created by zivilan on 27/11/2016.
 */

public class DaolabAnalyticsPlugin extends PKPlugin{
    private static final PKLog log = PKLog.get("DaolabAnalyticsPlugin");
    private static final String TAG = "DaolabAnalyticsPlugin";
    private final String DEFAULT_BASE_URL = "https://analytics.kaltura.com/api_v3/index.php";

    private int uiconfId;
    private String baseUrl;
    private int partnerId;


    public enum KAnalonyEvents {
        IMPRESSION(1),
        PLAY_REQUEST(2),
        PLAY(3),
        RESUME(4),
        PLAY_25PERCENT(11),
        PLAY_50PERCENT(12),
        PLAY_75PERCENT(13),
        PLAY_100PERCENT(14),
        PAUSE(33),
        REPLAY(34),
        SEEK(35),
        SOURCE_SELECTED(39),
        INFO(40),
        SPEED(41),
        VIEW(99);

        private final int value;

        KAnalonyEvents(int value) {
            this.value = value;
        }

        public int getValue() {
            return value;
        }
    }

    private Player player;
    private PKMediaConfig mediaConfig;
    private JsonObject pluginConfig;
    private MessageBus messageBus;
    private RequestQueue requestsExecutor;
    private java.util.Timer timer = new java.util.Timer();

    private float seekPercent = 0;
    private boolean playReached25 = false;
    private boolean playReached50 = false;
    private boolean playReached75 = false;
    private boolean playReached100 = false;
    private boolean isDvr = false;
    private int currentBitrate = -1;
    private int bufferTime = 0;
    private int eventIdx = 0;
    private boolean isFirstPlay = true;
    private boolean intervalOn = false;
    private boolean hasSeeked = false;
    private boolean isImpression = false;

    public static final Factory factory = new Factory() {
        @Override
        public String getName() {
            return "KalturaAnalytics";
        }

        @Override
        public PKPlugin newInstance() {
            return new DaolabAnalyticsPlugin();
        }

        @Override
        public void warmUp(Context context) {

        }
    };

    @Override
    protected void onLoad(Player player, Object config, final MessageBus messageBus, Context context) {
        messageBus.listen(mEventListener, (Enum[]) PlayerEvent.Type.values());
        this.requestsExecutor = APIOkRequestsExecutor.getSingleton();
        this.player = player;
        this.pluginConfig = (JsonObject) config;
        this.messageBus = messageBus;
    }

    @Override
    public void onDestroy() {
        resetPlayerFlags();
        intervalOn = false;
        timer.cancel();
    }

    @Override
    protected void onUpdateMedia(PKMediaConfig mediaConfig) {
        if (Utils.isJsonObjectValueValid(pluginConfig, "uiconfId")) {
            uiconfId = Integer.valueOf(pluginConfig.get("uiconfId").toString());
        } else {
            uiconfId = 0;
        }
        if (Utils.isJsonObjectValueValid(pluginConfig, "baseUrl")) {
            baseUrl = pluginConfig.getAsJsonPrimitive("baseUrl").getAsString();
        } else {
            baseUrl = DEFAULT_BASE_URL;
        }
        if (Utils.isJsonObjectValueValid(pluginConfig, "partnerId")) {
            partnerId = pluginConfig.getAsJsonPrimitive("partnerId").getAsInt();
        } else {
            partnerId = 0;
        }

        isFirstPlay = true;
        this.mediaConfig = mediaConfig;
        resetPlayerFlags();
    }

    @Override
    protected void onUpdateConfig(Object config) {
        this.pluginConfig = (JsonObject) config;
    }

    @Override
    protected void onApplicationPaused() {

    }

    @Override
    protected void onApplicationResumed() {

    }

    public void onEvent(PlayerEvent.StateChanged event) {
        switch (event.newState) {
            case IDLE:

                break;
            case LOADING:

                break;
            case READY:
                if (!isImpression){
                    sendAnalyticsEvent(KAnalonyEvents.IMPRESSION);
                    isImpression = true;
                }
                if (!intervalOn) {
                    intervalOn = true;
                    startTimeObservorInterval();
                }
                break;
            case BUFFERING:
                break;
        }
    }

    private PKEvent.Listener mEventListener = new PKEvent.Listener() {
        @Override
        public void onEvent(PKEvent event) {
            if (event instanceof PlayerEvent) {
                switch (((PlayerEvent) event).type) {
                    case STATE_CHANGED:
                        DaolabAnalyticsPlugin.this.onEvent((PlayerEvent.StateChanged) event);
                        break;
                    case CAN_PLAY:

                        break;
                    case DURATION_CHANGE:

                        break;
                    case ENDED:

                        break;
                    case ERROR:

                        break;
                    case LOADED_METADATA:

                        break;
                    case REPLAY:
                        sendAnalyticsEvent(KAnalonyEvents.REPLAY);
                        break;
                    case PAUSE:
                        sendAnalyticsEvent(KAnalonyEvents.PAUSE);
                        break;
                    case PLAY:
                        sendAnalyticsEvent(KAnalonyEvents.PLAY_REQUEST);
                        break;
                    case PLAYING:
                        if (isFirstPlay){
                            isFirstPlay = false;
                            sendAnalyticsEvent(KAnalonyEvents.PLAY);
                        } else {
                            sendAnalyticsEvent(KAnalonyEvents.RESUME);
                        }
                        break;
                    case SEEKED:
                        hasSeeked = true;
                        seekPercent = (float) player.getCurrentPosition() / player.getDuration();
                        sendAnalyticsEvent(KAnalonyEvents.SEEK);
                        break;
                    case SEEKING:

                        break;
                    default:

                        break;
                }
            }
        }
    };

    private void resetPlayerFlags() {
        seekPercent = 0;
        playReached25 = false;
        playReached50 = false;
        playReached75 = false;
        playReached100 = false;
        hasSeeked = false;
        eventIdx = 0;
        isFirstPlay = true;
    }

    private void startTimeObservorInterval() {
        int timerInterval = pluginConfig.has("timerInterval") ? pluginConfig.getAsJsonPrimitive("timerInterval").getAsInt() * (int) Consts.MILLISECONDS_MULTIPLIER : Consts.DEFAULT_ANALYTICS_TIMER_INTERVAL_LOW;
        if (timer == null) {
            timer = new java.util.Timer();
        }
        timer.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                float progress = (float) player.getCurrentPosition() / player.getDuration();
                if (progress >= 0.25 && !playReached25 && seekPercent <= 0.25) {
                    playReached25 = true;
                    sendAnalyticsEvent(KAnalonyEvents.PLAY_25PERCENT);
                } else if (progress >= 0.5 && !playReached50 && seekPercent < 0.5) {
                    playReached50 = true;
                    sendAnalyticsEvent(KAnalonyEvents.PLAY_50PERCENT);
                } else if (progress >= 0.75 && !playReached75 && seekPercent <= 0.75) {
                    playReached75 = true;
                    sendAnalyticsEvent(KAnalonyEvents.PLAY_75PERCENT);
                } else if (progress >= 0.98 && !playReached100 && seekPercent < 1) {
                    playReached100 = true;
                    sendAnalyticsEvent(KAnalonyEvents.PLAY_100PERCENT);
                }
            }
        }, 0, timerInterval);
    }

    private void sendAnalyticsEvent(final KAnalonyEvents eventType) {
        String sessionId = (player.getSessionId() != null) ? player.getSessionId() : "";
        String playbackType = isDvr ? "dvr" : "live";
        int flavourId = -1;

        RequestBuilder requestBuilder = AnalyticsService.sendAnalyticsEvent(baseUrl, partnerId, eventType.getValue(), PlayKitManager.CLIENT_TAG, playbackType,
                sessionId, player.getCurrentPosition(), uiconfId, mediaConfig.getMediaEntry().getId(), eventIdx++, flavourId, bufferTime, currentBitrate, "hls");

        requestBuilder.completion(new OnRequestCompletion() {
            @Override
            public void onComplete(ResponseElement response) {
                log.d("onComplete: " + eventType.toString());
            }
        });
        requestsExecutor.queue(requestBuilder.build());
        messageBus.post(new LogEvent(TAG + " " + eventType.toString(), requestBuilder.build().getUrl()));
    }
}

