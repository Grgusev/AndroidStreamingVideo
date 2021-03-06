

package com.daolab.daolabplayer.plugins.youbora;

import com.daolab.daolabplayer.MessageBus;
import com.daolab.daolabplayer.PKError;
import com.daolab.daolabplayer.PKEvent;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaConfig;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PlayKitManager;
import com.daolab.daolabplayer.PlaybackInfo;
import com.daolab.daolabplayer.Player;
import com.daolab.daolabplayer.PlayerEvent;
import com.daolab.daolabplayer.plugins.ads.AdCuePoints;
import com.daolab.daolabplayer.plugins.ads.AdEvent;
import com.daolab.daolabplayer.utils.Consts;
import com.npaw.youbora.plugins.PluginGeneric;
import com.npaw.youbora.youboralib.BuildConfig;
import com.npaw.youbora.youboralib.utils.Utils;

import org.json.JSONException;

import java.util.Map;

/**
 * @hide
 */

class YouboraLibraryManager extends PluginGeneric {

    private static final PKLog log = PKLog.get("YouboraLibraryManager");
    private static final String DAOLAO_ANDROID = "DAOLAB-Android";

    private Player player;
    private MessageBus messageBus;
    private PKMediaConfig mediaConfig;

    private boolean isFirstPlay = true;
    private boolean isBuffering = false;
    private boolean allowSendingYouboraBufferEvents = false; //When false will prevent from sending bufferUnderrun event.

    private String lastReportedResource = "unknown";
    private Double lastReportedBitrate = -1.0;
    private Double lastReportedThroughput;
    private String lastReportedRendition;
    private AdCuePoints adCuePoints;

    YouboraLibraryManager(String options) throws JSONException {
        super(options);
    }

    YouboraLibraryManager(Map<String, Object> options, MessageBus messageBus, PKMediaConfig mediaConfig, Player player) {
        super(options);
        this.player = player;
        this.messageBus = messageBus;
        this.mediaConfig = mediaConfig;

        messageBus.listen(mEventListener, (Enum[]) PlayerEvent.Type.values());
        messageBus.listen(mEventListener, (Enum[]) AdEvent.Type.values());
    }

    protected void init() {
        super.init();
        this.pluginName = DAOLAO_ANDROID;
        this.pluginVersion = BuildConfig.VERSION_NAME + "-" + getPlayerVersion();
    }

    private void onEvent(PlayerEvent.StateChanged event) {
        //If it is first play, do not continue with the flow.
        if (isFirstPlay) {
            return;
        }

        switch (event.newState) {
            case READY:
                if (isBuffering) {
                    isBuffering = false;
                    bufferedHandler();
                }
                break;
            case BUFFERING:
                if (allowSendingYouboraBufferEvents) {
                    isBuffering = true;
                    bufferingHandler();
                } else {
                    allowSendingYouboraBufferEvents = true;
                }
                break;
            default:
                break;
        }
        sendReportEvent(event);
    }


    private PKEvent.Listener mEventListener = new PKEvent.Listener() {
        @Override
        public void onEvent(PKEvent event) {

            if (event.eventType() == PlayerEvent.Type.PLAYBACK_INFO_UPDATED) {
                PlaybackInfo currentPlaybackInfo = ((PlayerEvent.PlaybackInfoUpdated) event).playbackInfo;
                lastReportedBitrate = Long.valueOf(currentPlaybackInfo.getVideoBitrate()).doubleValue();
                lastReportedThroughput = Long.valueOf(currentPlaybackInfo.getVideoThroughput()).doubleValue();
                lastReportedResource = currentPlaybackInfo.getMediaUrl();
                lastReportedRendition = generateRendition(lastReportedBitrate, (int) currentPlaybackInfo.getVideoWidth(), (int) currentPlaybackInfo.getVideoHeight());
                return;
            }

            if (event instanceof PlayerEvent && viewManager != null) {
                log.d("PlayerEvent: " + ((PlayerEvent) event).type.toString());
                switch (((PlayerEvent) event).type) {
                    case DURATION_CHANGE:
                        log.d("new duration = " + ((PlayerEvent.DurationChanged) event).duration);
                        break;
                    case STATE_CHANGED:
                        YouboraLibraryManager.this.onEvent((PlayerEvent.StateChanged) event);
                        break;
                    case ENDED:
                        if (!isFirstPlay && ((adCuePoints == null) || !adCuePoints.hasPostRoll())) {
                            endedHandler();
                            isFirstPlay = true;
                            adCuePoints = null;
                        }
                        break;
                    case ERROR:
                        sendErrorHandler(event);
                        adCuePoints = null;
                        break;
                    case PAUSE:
                        pauseHandler();
                        break;
                    case PLAY:
                        if (!isFirstPlay) {
                            resumeHandler();
                        } else {
                            isFirstPlay = false;
                            playHandler();
                        }
                        break;
                    case PLAYING:
                        if (isFirstPlay) {
                            isFirstPlay = false;
                            playHandler();
                        }
                        playingHandler();
                        break;
                    case SEEKED:
                        seekedHandler();
                        break;
                    case SEEKING:
                        seekingHandler();
                        break;
                    default:
                        break;
                }
                if (((PlayerEvent) event).type != PlayerEvent.Type.STATE_CHANGED) {
                    sendReportEvent(event);
                }
            } else if (event instanceof AdEvent) {
                onAdEvent((AdEvent) event);
            }
        }
    };

    private void sendErrorHandler(PKEvent event) {

        PlayerEvent.Error errorEvent = (PlayerEvent.Error) event;
        String errorMsg = "Player error occurred.";
        PKError error = errorEvent.error;
        if (error.cause == null) {
            errorHandler(errorMsg, event.eventType().toString());
            return;
        }

        Exception playerErrorException = (Exception) error.cause;
        String errorMetadata = errorMsg;
        String exceptionClass = "";
        String exceptionCause = "";
        if (playerErrorException.getCause() != null && playerErrorException.getCause().getClass() != null) {
            exceptionClass = playerErrorException.getCause().getClass().getName();
            errorMetadata = (playerErrorException.getCause().toString() != null) ? playerErrorException.getCause().toString() : "NA";
            exceptionCause = playerErrorException.toString();
        }
        errorHandler(exceptionCause, exceptionClass, errorMetadata);
    }

    private void onAdEvent(AdEvent event) {
        if (event.type != AdEvent.Type.PLAY_HEAD_CHANGED) {
            log.d("Ad Event: " + event.type.name());
        }

        switch (event.type) {
            case STARTED:
                ignoringAdHandler();
                allowSendingYouboraBufferEvents = false;
                break;
            case CONTENT_RESUME_REQUESTED:
                ignoredAdHandler();
                break;
            case CUEPOINTS_CHANGED:
                AdEvent.AdCuePointsUpdateEvent cuePointsList = (AdEvent.AdCuePointsUpdateEvent) event;
                adCuePoints = cuePointsList.cuePoints;
                break;
            case ALL_ADS_COMPLETED:
                if (adCuePoints != null && adCuePoints.hasPostRoll()) {
                    endedHandler();
                    isFirstPlay = true;
                    adCuePoints = null;
                }
                break;
            default:
                break;
        }
    }

    @Override
    public void pauseMonitoring() {
        super.pauseMonitoring();
        allowSendingYouboraBufferEvents = false;
    }

    public void startMonitoring(Object player) {
        log.d("startMonitoring");
        super.startMonitoring(player);
        isFirstPlay = true;
        allowSendingYouboraBufferEvents = false;
    }

    public void stopMonitoring() {
        log.d("stopMonitoring");
        super.stopMonitoring();
    }

    public Double getBitrate() {
        return this.lastReportedBitrate;
    }

    public Double getThroughput() {
        log.d("getThroughput = " + lastReportedThroughput);
        return this.lastReportedThroughput;
    }

    public String getRendition() {
        log.d("getRendition = " + lastReportedRendition);
        return lastReportedRendition;
    }

    public String getPlayerVersion() {
        return Consts.Daolab + "-" + PlayKitManager.CLIENT_TAG;
    }

    public Double getPlayhead() {
        double currPos = Long.valueOf(player.getCurrentPosition() / Consts.MILLISECONDS_MULTIPLIER).doubleValue();
        log.d("getPlayhead currPos = " + currPos);
        return (currPos >= 0) ? currPos : 0;
    }

    public String getResource() {
        return lastReportedResource;
    }

    public Double getMediaDuration() {
        double lastReportedMediaDuration = (mediaConfig == null) ? 0 : Long.valueOf(mediaConfig.getMediaEntry().getDuration() / Consts.MILLISECONDS_MULTIPLIER).doubleValue();
        log.d("lastReportedMediaDuration = " + lastReportedMediaDuration);
        return lastReportedMediaDuration;
    }

    public String getTitle() {
        if (mediaConfig == null || mediaConfig.getMediaEntry() == null) {
            return "unknown";
        } else {
            return mediaConfig.getMediaEntry().getId();
        }
    }

    public Boolean getIsLive() {
        return mediaConfig != null && (mediaConfig.getMediaEntry().getMediaType() == PKMediaEntry.MediaEntryType.Live);
    }

    private void sendReportEvent(PKEvent event) {
        String reportedEventName = event.eventType().name();
        messageBus.post(new YouboraEvent.YouboraReport(reportedEventName));
    }

    public String generateRendition(double bitrate, int width, int height) {

        if ((width <= 0 || height <= 0) && bitrate <= 0) {
            return super.getRendition();
        } else {
            return Utils.buildRenditionString(width, height, bitrate);
        }
    }

    public void resetValues() {
        lastReportedBitrate = super.getBitrate();
        lastReportedRendition = super.getRendition();
        lastReportedThroughput = super.getThroughput();
        isFirstPlay = true;
    }

    public void onUpdateConfig() {
        resetValues();
        adCuePoints = null;
        lastReportedResource = "unknown";
    }
}
