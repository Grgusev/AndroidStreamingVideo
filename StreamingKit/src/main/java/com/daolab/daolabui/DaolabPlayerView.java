package com.daolab.daolabui;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.hardware.SensorManager;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;

import com.daolab.daolabplayer.MediaEntryProvider;
import com.daolab.daolabplayer.PKEvent;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.PKMediaConfig;
import com.daolab.daolabplayer.PKMediaEntry;
import com.daolab.daolabplayer.PKMediaFormat;
import com.daolab.daolabplayer.PKMediaSource;
import com.daolab.daolabplayer.PKPluginConfigs;
import com.daolab.daolabplayer.PlayKitManager;
import com.daolab.daolabplayer.Player;
import com.daolab.daolabplayer.PlayerEvent;
import com.daolab.daolabplayer.PlayerState;
import com.daolab.daolabplayer.mediaproviders.base.OnMediaLoadCompletion;
import com.daolab.daolabplayer.mediaproviders.mock.MockMediaProvider;
import com.daolab.daolabplayer.player.BaseTrack;
import com.daolab.daolabplayer.player.PKTracks;
import com.daolab.daolabplayer.player.TextTrack;
import com.daolab.daolabplayer.player.VideoTrack;
import com.daolab.daolabplayer.plugins.ads.AdCuePoints;
import com.daolab.daolabplayer.plugins.ads.AdEvent;
import com.daolab.daolabplayer.plugins.ads.ima.IMAConfig;
import com.daolab.daolabplayer.plugins.ads.ima.IMAPlugin;
import com.daolab.daolabplayer.plugins.playback.DaolabPlaybackRequestAdapter;
import com.daolab.daolabplayer.utils.Consts;
import com.daolab.netkit.connect.response.ResultElement;
import com.daolab.daolabplayer.R;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.Formatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Created by almond on 8/4/2017.
 */

public class DaolabPlayerView extends LinearLayout implements DaolabPlayer {

    private Context mContext;
    private Activity parentActivity = null;

    private static final PKLog log = PKLog.get("DaolabPlayerView");

    private Player player;
    private MediaEntryProvider mediaProvider;
    private PlaybackControlsView controlsView;
    public boolean nowPlaying;
    private boolean isFullscreen = false;
    private boolean isInline = true;
    private boolean isFullscreenActivity = false;
    private String hlsURL = "";
    private boolean isThumbShow = true;
    private boolean isTrackInfoShow = true;

    private boolean isTrackSet  = false;

    private boolean isPlayingAD = false;

    PowerManager.WakeLock mWakeLock;

    private String fromTime    = "";

    private DaolabVideoPlayerEventListener mEventListener = null;

    /**********Google Double Ad Enable
     *
     */
    private String mGoogleAdUrl         = "";

    //https://pubads.g.doubleclick.net/gampad/ads?sz=640x480&iu=/124319096/external/ad_rule_samples&ciu_szs=300x250&ad_rule=1&impl=s&gdfp_req=1&env=vp&output=vmap&unviewed_position_start=1&cust_params=deployment%3Ddevsite%26sample_ar%3Dpremidpostoptimizedpodbumper&cmsid=496&vid=short_onecue&correlator=

    ProgressBar progressBar;

    PKMediaConfig mediaConfig = null;

    public boolean isPrepared   = false;
    public boolean firstPlay    = false;

    private static DaolabPlayerView mInstance;

    public static DaolabPlayerView getInstance(Context context)
    {
        if (mInstance == null)
        {
            mInstance = new DaolabPlayerView(context);
        }

        return mInstance;
    }

    Handler controlViewHandler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            showControlView(false);
        }
    };

    private LinearLayout mPlayerRoot;
    private ImageView mThumbView;

    public boolean AUTO_PLAY_ON_RESUME = true;

    public int isLandscape  = -1; // -1 : unknown, 0: landscape, 1:portrait

    OnMediaLoadCompletion playLoadedEntry = new OnMediaLoadCompletion() {
        @Override
        public void onComplete(final ResultElement<PKMediaEntry> response) {
            mPlayerRoot.post(new Runnable() {
                @Override
                public void run() {
                    if (response.isSuccess()) {
                        onMediaLoaded(response.getResponse());
                    }
                    else {
                    }
                }
            });
        }
    };

    OrientationEventListener orientationListener = null;

    public DaolabPlayerView(Context context) {
        this(context, null);
    }

    public DaolabPlayerView(Context context, @Nullable AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DaolabPlayerView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;

        if (mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT)
        {
            isLandscape = 0;
        }
        else
            isLandscape = 1;

        orientationListener = new OrientationEventListener(context, SensorManager.SENSOR_DELAY_UI) {

            public void onOrientationChanged(int orientation) {
                if (isLandscape(orientation)) {
                    if (isLandscape == 0) return;
                    isLandscape = 0;
                    HashMap<String, Object> mParams = new HashMap<String, Object>();
                    mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

                    callScreenEventListener(DaolabUIData.DaoLabScreenEventTypeLandscape, mParams);
                }
                else
                {
                    if (isLandscape == 1) return;
                    isLandscape = 1;
                    HashMap<String, Object> mParams = new HashMap<String, Object>();
                    mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

                    callScreenEventListener(DaolabUIData.DaoLabScreenEventTypePortrait, mParams);
                }
            }

            private boolean isLandscape(int orientation){
                return mContext.getResources().getConfiguration().orientation != Configuration.ORIENTATION_PORTRAIT;
            }
        };

        orientationListener.enable();

        LayoutInflater.from(context).inflate(R.layout.playviewer_layout, this);
        progressBar = (ProgressBar) findViewById(R.id.progressBar);

        final PowerManager pm = (PowerManager) mContext.getSystemService(Context.POWER_SERVICE);
        mWakeLock = pm.newWakeLock(PowerManager.SCREEN_DIM_WAKE_LOCK, "Screen Lock");

        registerPlugins();

        mPlayerRoot = (LinearLayout)findViewById(R.id.player_root);

        PKPluginConfigs pluginConfig = new PKPluginConfigs();

        player = PlayKitManager.loadPlayer(mContext, pluginConfig);
        DaolabPlaybackRequestAdapter.install(player, "myApp"); // in case app developer wants to give customized referrer instead the default referrer in the playmanifest

        mThumbView = (ImageView)findViewById(R.id.thumbnail);

        controlsView = (PlaybackControlsView) this.findViewById(R.id.playerControls);
        controlsView.setPlayer(player);
        controlsView.setViewer(this);
        showProgressbar(View.INVISIBLE);

        showThumbnail(true);

        if (isTrackInfoShow == true)
        {
            controlsView.setTrackSectionVisibility(View.VISIBLE);
        }
        else
        {
            controlsView.setTrackSectionVisibility(View.INVISIBLE);
        }

        mPlayerRoot.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!controlsView.getVisible())
                {
                    showControlView(true);
                }
                else
                {
                    showControlView(false);
                }
            }
        });

        mThumbView.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                if (nowPlaying)
                {
                    pausePlay();
                }
                else
                {
                    playStart();
                }
            }
        });

        addPlayerListeners(progressBar);

        isFullscreen = false;

        mPlayerRoot.addView(player.getView());
    }

    public void playStart()
    {
        if (mediaConfig == null) return;
        player.prepare(mediaConfig);
        isPrepared = true;
        resumePlay();
    }

    public void showBackIcon(boolean visible)
    {
        if (controlsView != null)
        {
            controlsView.showBackIcon(visible);
        }
    }

    private void showProgressbar(int visible)
    {
        if (visible == View.VISIBLE)
        {
            controlsView.setPlayButtonVisible(View.GONE);
        }
        else
        {
            controlsView.setPlayButtonVisible(View.VISIBLE);
        }
        progressBar.setVisibility(visible);
    }

    public void callPlayEventListener(String name, Map<String, Object> mParams)
    {
        android.util.Log.e("almond", name);
        if (mEventListener != null)
        {
            mEventListener.onPlaybackChange(name, mParams);
        }
    }

    public void callScreenEventListener(String name, Map<String, Object> mParams)
    {
        android.util.Log.e("almond", name);
        if (mEventListener != null)
        {
            mEventListener.onScreenChange(name, mParams);
        }
    }

    public void callAudioEventListener(String name, Map<String, Object> mParams)
    {
        android.util.Log.e("almond", name);
        if (mEventListener != null)
        {
            mEventListener.onAudioChange(name, mParams);
        }
    }

    public void callSubtitleChange(String name, Map<String, Object> mParams)
    {
        android.util.Log.e("almond", name);
        if (mEventListener != null)
        {
            mEventListener.onSubtitleChange(name, mParams);
        }
    }

    public void callQualityChange(String name, Map<String, Object> mParams)
    {
        android.util.Log.e("almond", name);
        if (mEventListener != null)
        {
            mEventListener.onQualityChange(name, mParams);
        }
    }

    private void registerPlugins() {
        PlayKitManager.registerPlugins(mContext, IMAPlugin.factory);
    }

    @Override
    public void showThumbnail(boolean visible)
    {
        if (visible)
        {
            isThumbShow = true;
            mPlayerRoot.setVisibility(View.INVISIBLE);
            mThumbView.setVisibility(View.VISIBLE);

            showControlView(false);

            if (controlsView != null)
                controlsView.setViewOnThumbnail();
        }
        else
        {
            isThumbShow = false;
            mPlayerRoot.setVisibility(View.VISIBLE);
            mThumbView.setVisibility(View.INVISIBLE);

            showControlView(true);
            if (controlsView != null)
                controlsView.setViewNotThumbnail();
        }
    }

    @Override
    public void setTrackInfoShow(boolean visible) {
        isTrackInfoShow = visible;
        if (visible == true)
        {
            controlsView.setTrackSectionVisibility(View.VISIBLE);
        }
        else
        {
            controlsView.setTrackSectionVisibility(View.INVISIBLE);
        }
    }

    private void setIMAPluginConfig(PKPluginConfigs config)
    {
        IMAConfig adsConfig = new IMAConfig().setAdTagURL(mGoogleAdUrl);
        config.setPluginConfig(IMAPlugin.factory.getName(), adsConfig.toJSONObject());
    }

    @Override
    public void setGoogleAdLink(String tagUrl) {
        mGoogleAdUrl    = tagUrl;

        if (!TextUtils.isEmpty(tagUrl) && player != null)
        {
            PKPluginConfigs configs = new PKPluginConfigs();
            setIMAPluginConfig(configs);

            player.updatePluginConfig(IMAPlugin.factory.getName(), configs);
        }

    }

    @Override
    public void setVideoPlayerEventListener(DaolabVideoPlayerEventListener listener) {
        mEventListener = listener;
    }

    @Override
    public void setScreenOrientation(int orientation)
    {
        if (!isFullscreenActivity) return;

        if (orientation == DaolabUIData.SCREEN_ORIENTATION_LANDSCAPE)
        {
            parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

            HashMap<String, Object> mParams = new HashMap<String, Object>();
            mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

            callScreenEventListener(DaolabUIData.DaoLabScreenEventTypeLandscape, mParams);
            orientationListener.disable();
        }
        else
        {
            parentActivity.setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR);
            orientationListener.enable();
        }
    }

    @Override
    public void setVolume(float volume) {
        player.setVolume(volume);
    }

    @Override
    public float getVolume() {
        return player.getVolume();
    }

    @Override
    public void setMute() {
        player.setVolume(0f);
    }

    @Override
    public Boolean isMute() {
        float volume = player.getVolume();
        if (volume == 0f) return true;
        return false;
    }

    @Override
    public void setThumbnailUrl(String url) {
        if (mThumbView != null && !TextUtils.isEmpty(url))
        {
            Picasso.with(mContext).load(url).into(mThumbView);
        }
    }

    @Override
    public void setThumbnailResource(int resourceId) {
        mThumbView.setImageResource(resourceId);
    }

    public void showControlView(boolean visible) {
        if (controlsView == null) return;

        if (isPlayingAD == true)
        {
            controlsView.setVisibility(View.VISIBLE);
            controlsView.showAdControlView(true);
            controlViewHandler.removeMessages(DaolabUIData.MSG_CONTROL_VIEW);
            controlsView.showVisible(false);
            return;
        }

        controlsView.showAdControlView(false);

        if (visible == true)
        {
            if (controlsView.getVisible() == false)
            {
                controlsView.setVisibility(View.VISIBLE);
                controlsView.setControllerVisibility(View.VISIBLE);
                controlsView.setVideoSectionVisibility(View.GONE);
                controlsView.setSubtitleSectionVisibility(View.GONE);
            }

            controlViewHandler.removeMessages(DaolabUIData.MSG_CONTROL_VIEW);
            controlViewHandler.sendEmptyMessageDelayed(DaolabUIData.MSG_CONTROL_VIEW, DaolabUIData.SHOW_CONTROL_TIME);
        }
        else
        {
            if (controlsView.getVisible() == true)
            {
                controlsView.setVisibility(View.GONE);
                controlsView.setControllerVisibility(View.VISIBLE);
                controlsView.setVideoSectionVisibility(View.GONE);
                controlsView.setSubtitleSectionVisibility(View.GONE);

            }
        }
        controlsView.showVisible(visible);
    }

    private String stringForTime(long timeMs) {

        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        StringBuilder formatBuilder = new StringBuilder();
        Formatter formatter = new Formatter(formatBuilder, Locale.getDefault());
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    private void addPlayerListeners(final ProgressBar appProgressBar) {

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                log.d("AD_CONTENT_PAUSE_REQUESTED");
                showProgressbar(View.VISIBLE);

                showControlView(false);

                if (controlsView != null)
                {
                    controlsView.getBackupDeviceDefaultVolume();
                }
            }
        }, AdEvent.Type.CONTENT_PAUSE_REQUESTED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                AdEvent.AdCuePointsUpdateEvent cuePointsList = (AdEvent.AdCuePointsUpdateEvent) event;
                AdCuePoints adCuePoints = cuePointsList.cuePoints;
                if (adCuePoints != null) {
                    log.d("Has Postroll = " + adCuePoints.hasPostRoll());
                }
            }
        }, AdEvent.Type.CUEPOINTS_CHANGED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                log.d("AD_STARTED");
                showProgressbar(View.INVISIBLE);
                isPlayingAD = true;
                showControlView(false);
            }
        }, AdEvent.Type.STARTED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                log.d("Ad Event AD_RESUMED");
                nowPlaying = true;
                isPlayingAD = true;
                showControlView(false);
                showProgressbar(View.INVISIBLE);
            }
        }, AdEvent.Type.RESUMED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                log.d("Ad Event AD_ALL_ADS_COMPLETED");
                showProgressbar(View.INVISIBLE);
                isPlayingAD = false;
                showControlView(true);

            }
        }, AdEvent.Type.ALL_ADS_COMPLETED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                showProgressbar(View.INVISIBLE);
                isPlayingAD = false;
                showControlView(true);

                if (controlsView != null)
                {
                    controlsView.setBackupDeviceDefaultVolume();
                }
            }
        }, AdEvent.Type.CONTENT_RESUME_REQUESTED);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                nowPlaying = true;
                isPlayingAD = false;
                showControlView(true);

                if (controlsView != null)
                {
                    controlsView.setBackupDeviceDefaultVolume();
                }
            }
        }, AdEvent.Type.SKIPPED);


        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                nowPlaying = true;
                if(controlsView != null){
                    controlsView.setPlayingState(PlayerEvent.Type.PLAY);
                }
                if (!mWakeLock.isHeld())
                {
                    mWakeLock.acquire();
                }
            }
        }, PlayerEvent.Type.PLAY);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                if (firstPlay == false)
                {
                    HashMap<String, Object> mParams = new HashMap<String, Object>();
                    mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

                    callPlayEventListener(DaolabUIData.DaoLabPlayEventTypePlayStart, mParams);
                }

                firstPlay = true;
            }
        }, PlayerEvent.Type.PLAYING);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                nowPlaying = false;
                if(controlsView != null){
                    controlsView.setPlayingState(PlayerEvent.Type.PAUSE);
                }
                if (mWakeLock.isHeld())
                {
                    mWakeLock.release();
                }

                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

                callPlayEventListener(DaolabUIData.DaoLabPlayEventTypePause, mParams);

            }
        }, PlayerEvent.Type.PAUSE);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);
                mParams.put(DaolabUIData.DaoLabPlayEventTypeSeekPosition, player.getCurrentPosition());
                mParams.put(DaolabUIData.DaoLabPlayEventTypeFromTime, fromTime);
                mParams.put(DaolabUIData.DaoLabPlayEventTypeToTime, stringForTime(player.getCurrentPosition()));


                if (nowPlaying == false)
                {
                    callPlayEventListener(DaolabUIData.DaoLabPlayEventTypeSeekTo, mParams);
                }
                else
                {
                    callPlayEventListener(DaolabUIData.DaoLabPlayEventTypeSeekToPlay, mParams);
                }


            }
        }, PlayerEvent.Type.SEEKED);

        player.addStateChangeListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
            if (event instanceof PlayerEvent.StateChanged) {
                PlayerEvent.StateChanged stateChanged = (PlayerEvent.StateChanged) event;
                log.d("State changed from " + stateChanged.oldState + " to " + stateChanged.newState);

                if (stateChanged.newState == PlayerState.BUFFERING)
                {
                    showProgressbar(View.VISIBLE);
                }
                else{
                    showProgressbar(View.INVISIBLE);
                }

                if(controlsView != null){
                    controlsView.setPlayerState(stateChanged.newState);
                }
            }
            }
        });

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                //When the track data available, this event occurs. It brings the info object with it.
                PlayerEvent.TracksAvailable tracksAvailable = (PlayerEvent.TracksAvailable) event;
                if (!isTrackSet) {
                    populateSpinnersWithTrackInfo(tracksAvailable.tracksInfo);
                    isTrackSet = true;
                }
            }
        }, PlayerEvent.Type.TRACKS_AVAILABLE);

        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                //When the track data available, this event occurs. It brings the info object with it.
                PlayerEvent.PlaybackInfoUpdated info = (PlayerEvent.PlaybackInfoUpdated) event;

                if (info.playbackInfo.getIsLiveStream())
                {
                    controlsView.setLive(true);
                }
                else
                {
                    controlsView.setLive(false);
                }
            }
        }, PlayerEvent.Type.PLAYBACK_INFO_UPDATED);
    }

    /**
     * populating spinners with track info.
     *
     * @param tracksInfo - the track info.
     */

    TrackItem[] videoTrackItems, subtitlesTrackItems;

    private void populateSpinnersWithTrackInfo(PKTracks tracksInfo) {

        //Retrieve info that describes available tracks.(video/audio/subtitle).
        videoTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_VIDEO, tracksInfo.getVideoTracks());
        //populate spinner with this info.

        TrackItemAdapter videoTrackAdapter = new TrackItemAdapter(mContext, videoTrackItems);
        controlsView.setVideoTrackAdapter(videoTrackAdapter);
        videoTrackAdapter.setSelectedPos(tracksInfo.getDefaultVideoTrackIndex());
        videoTrackAdapter.setOnItemClickListener(new TrackItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                controlsView.setVideoSectionVisibility(View.GONE);
                controlsView.setControllerVisibility(View.VISIBLE);

                controlsView.setVideoTrackText(videoTrackItems[position].getTrackName());

                TrackItem trackItem = videoTrackItems[position];
                //tell to the player, to switch track based on the user selection.
                player.changeTrack(trackItem.getUniqueId());

                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);
                mParams.put(DaolabUIData.DaoLabPlayerResolutionId, videoTrackItems[position]);

                callQualityChange(DaolabUIData.DaoLabPlayEventTypeDidResolution, mParams);
            }
        });

        subtitlesTrackItems = obtainRelevantTrackInfo(Consts.TRACK_TYPE_TEXT, tracksInfo.getTextTracks());
        TrackItemAdapter subtitleTrackAdapter = new TrackItemAdapter(mContext, subtitlesTrackItems);
        controlsView.setSubtitleTrackAdapter(subtitleTrackAdapter);
        subtitleTrackAdapter.setSelectedPos(tracksInfo.getDefaultTextTrackIndex());
        subtitleTrackAdapter.setOnItemClickListener(new TrackItemAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                controlsView.setSubtitleSectionVisibility(View.GONE);
                controlsView.setControllerVisibility(View.VISIBLE);

                TrackItem trackItem = subtitlesTrackItems[position];
                //tell to the player, to switch track based on the user selection.
                player.changeTrack(trackItem.getUniqueId());

                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);
                mParams.put(DaolabUIData.DaoLabPlayerSubtitleId, subtitlesTrackItems[position]);

                callSubtitleChange(DaolabUIData.DaoLabPlayEventTypeDidSubtitle, mParams);
            }
        });
    }

    /**
     * Obtain info that user is interested in.
     * For example if user want to display in UI bitrate of the available tracks,
     * he can do it, by obtaining the tackType of video, and getting the getBitrate() from videoTrackInfo.
     *
     * @param trackType  - tyoe of the track you are interested in.
     * @param trackInfos - all availables tracks.
     * @return
     */
    private TrackItem[] obtainRelevantTrackInfo(int trackType, List<? extends BaseTrack> trackInfos) {
        TrackItem[] trackItems = null;
        switch (trackType) {
            case Consts.TRACK_TYPE_VIDEO:
                for (int i = 0; i < trackInfos.size(); i ++)
                {
                    VideoTrack videoTrackInfo = (VideoTrack) trackInfos.get(i);
                    if (videoTrackInfo.getHeight() == 1080)
                    {
                        trackInfos.remove(i);
                        i --;
                    }
                    else if (videoTrackInfo.getHeight() == 0 && videoTrackInfo.getBitrate() == 4400000L)
                    {
                        trackInfos.remove(i);
                        i --;
                    }
                }
                trackItems = new TrackItem[trackInfos.size()];


                if (trackInfos.isEmpty() || trackInfos.size() == 1)
                {
                    controlsView.setVideoTrackVisibility(View.INVISIBLE);
                }
                else
                {
                    controlsView.setVideoTrackVisibility(View.VISIBLE);
                }

                for (int i = 0; i < trackInfos.size(); i++) {
                    VideoTrack videoTrackInfo = (VideoTrack) trackInfos.get(i);
                    if(videoTrackInfo.isAdaptive()){
                        trackItems[i] = new TrackItem("Auto", videoTrackInfo.getUniqueId());
                    }else{
                        if (videoTrackInfo.getHeight() >= 0 )
                        {
                            trackItems[i] = new TrackItem(videoTrackInfo.getHeight() + "P", videoTrackInfo.getUniqueId());
                        }
                        else
                        {
                            String name = "Unknown";
                            long bitRate = videoTrackInfo.getBitrate();

                            if (bitRate == 1000000L)
                            {
                                name = "360P";
                            }
                            else if (bitRate == 1500000L)
                            {
                                name = "480P";
                            }
                            else if (bitRate == 2200000L)
                            {
                                name = "720p";
                            }
                            else if (bitRate == 4400000L)
                            {
                                name = "1080p";
                            }

                            trackItems[i] = new TrackItem(name, videoTrackInfo.getUniqueId());
                        }
                    }
                }

                break;
            case Consts.TRACK_TYPE_TEXT:
                trackItems = new TrackItem[trackInfos.size()];
                if (trackInfos.isEmpty() || trackInfos.size() == 1)
                {
                    controlsView.setSubtitleTrackVisibility(View.GONE);
                }
                else
                {
                    controlsView.setSubtitleTrackVisibility(View.VISIBLE);
                }

                for (int i = 0; i < trackInfos.size(); i++) {
                    TextTrack textTrackInfo = (TextTrack) trackInfos.get(i);
                    String lang = (textTrackInfo.getLabel() != null) ? textTrackInfo.getLabel() : "Unknown";
                    trackItems[i] = new TrackItem(lang, textTrackInfo.getUniqueId());
                }
                break;
        }
        return trackItems;
    }


    private void onMediaLoaded(PKMediaEntry mediaEntry) {

        if (mediaConfig == null) {
            mediaConfig = new PKMediaConfig().setMediaEntry(mediaEntry).setStartPosition(0);
        }

        player.prepare(mediaConfig);
        isPrepared = true;

        player.play();
        if (controlsView != null)
        {
            controlsView.setPlayerActive();
        }

        showThumbnail(false);
    }

    private void startMediaLoading(OnMediaLoadCompletion completion) {

        mediaProvider = new MockMediaProvider("mock/entries.playkit.json", mContext, "hls");

        mediaProvider.load(completion);
    }

    public void setFromTime(String from)
    {
        fromTime = from;
    }

    public void setFullscreenActivity(boolean isActivity)
    {
        isFullscreenActivity = isActivity;
    }

    public void setExitFullScreenTrigger()
    {
        HashMap<String, Object> mParams = new HashMap<String, Object>();
        mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

        callScreenEventListener(DaolabUIData.DaoLabScreenEventTypeExitFullScreen, mParams);
    }

    public void setEnterFullScreenTrigger()
    {
        HashMap<String, Object> mParams = new HashMap<String, Object>();
        mParams.put(DaolabUIData.DaoLabPlayerID, DaolabPlayerView.this);

        callScreenEventListener(DaolabUIData.DaoLabScreenEventTypeEnterFullScreen, mParams);
    }

    private void showFullscreen(boolean fullscreen) {

        if (isPlayingAD) return;

        if (!isFullscreenActivity)
        {
            if (fullscreen == true)
            {
                Intent intent = new Intent(parentActivity, VideoFullScreen.class);

                DaolabUIData.FULLSCREEN_PLAYER = this;
                intent.putExtra("playing", nowPlaying);

                setEnterFullScreenTrigger();

                mContext.startActivity(intent);
            }
            else
            {
                parentActivity.finish();
            }
        }
        else
        {
            if (fullscreen == true) {
                isFullscreen = true;
                controlsView.setFullscreenImage(isFullscreen);
                setEnterFullScreenTrigger();

                parentActivity.getWindow().addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
            else {
                isFullscreen = false;
                controlsView.setFullscreenImage(isFullscreen);
                setExitFullScreenTrigger();

                parentActivity.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
            }
        }
    }

    @Override
    public void setActivity(Activity activity)
    {
        parentActivity = activity;
    }

    @Override
    public void toggleFullscreen()
    {
        if (isFullscreen) showFullscreen(false);
        else showFullscreen(true);
    }

    @Override
    public void startPlayEntry()
    {
        startMediaLoading(playLoadedEntry);
    }

    @Override
    public void startPlay()
    {
        player.prepare(mediaConfig);
        isPrepared = true;

        player.play();
        if (controlsView != null)
        {
            controlsView.setPlayerActive();
        }

        nowPlaying = true;

        showThumbnail(false);
    }

    @Override
    public void prepare() {
        player.prepare(mediaConfig);
        isPrepared = true;
    }

    @Override
    public void pausePlay() {
        if (player == null) return;

        if (nowPlaying == true)
        {
            player.pause();
            controlsView.setPlayingState(PlayerEvent.Type.PAUSE);
            nowPlaying = false;
        }
    }

    @Override
    public void resumePlay() {
        if (player == null) return;

        if (nowPlaying == false)
        {
            player.play();
            if (controlsView != null)
            {
                controlsView.setPlayerActive();
            }
            controlsView.setPlayingState(PlayerEvent.Type.PLAY);
            showThumbnail(false);
            nowPlaying = true;

            if (controlsView != null)
            {
                controlsView.setPlayerActive();
            }
        }
    }

    @Override
    public void setHLSURL(String url, String id, String name)
    {
        if (mediaConfig == null) {
            mediaConfig = new PKMediaConfig();
        }

        PKMediaEntry entry = new PKMediaEntry();

        if (TextUtils.isEmpty(id)) {
            entry.setId("");
        }
        else {
            entry.setId(id);
        }

        if (TextUtils.isEmpty(name)) {
            entry.setName("");
        }
        else {
           entry.setName(name);
        }

        ArrayList<PKMediaSource> list = new ArrayList<>();
        PKMediaSource source = new PKMediaSource();
        if (TextUtils.isEmpty(id)) {
            source.setId("");
        }
        else {
            source.setId(id);
        }
        source.setMediaFormat(PKMediaFormat.hls);
        source.setUrl(url);
        hlsURL = url;
        list.add(source);

        entry.setSources(list);
        mediaConfig.setMediaEntry(entry);
    }

    @Override
    public void onPause()
    {
        if (controlsView != null) {
            controlsView.release();
        }
        if (player != null && isThumbShow == false) {
            player.onApplicationPaused();
        }

        if (orientationListener != null)
        {
            orientationListener.disable();
        }

    }

    public void exitFullScreenPlaying()
    {
        if (this.equals(DaolabUIData.FULLSCREEN_PLAYER) || isFullscreen == false)
        {
            this.setActivity(parentActivity);
            if (DaolabUIData.PLAYING_STATUS == 1) resumePlay();
            else pausePlay();

            DaolabUIData.FULLSCREEN_PLAYER = null;
            DaolabUIData.PLAYING_STATUS = -1;
        }
    }

    @Override
    public void onResume()
    {
        if (player != null && mediaConfig != null && isThumbShow == false) {
            player.onApplicationResumed();

            if (nowPlaying && AUTO_PLAY_ON_RESUME) {
                player.play();
                if (controlsView != null)
                {
                    controlsView.setPlayerActive();
                }
                nowPlaying = true;
                controlsView.setPlayingState(PlayerEvent.Type.PLAY);
                showThumbnail(false);
            }
        }
        if (controlsView != null) {
            controlsView.resume();
        }

        if (orientationListener != null)
        {
            orientationListener.enable();
        }
    }

    @Override
    public void setAutoPlayResume(boolean play)
    {
        AUTO_PLAY_ON_RESUME = play;
    }

    @Override
    public void setInline(boolean inline) {
        isInline = inline;
        if (controlsView != null)
        {
            controlsView.setInline(inline);
        }
    }

    @Override
    public void setFullScreen(boolean fullScreen) {
        isFullscreen = fullScreen;
        controlsView.setFullscreenImage(isFullscreen);
    }

    @Override
    public String getStatus() {
        if (nowPlaying) return "playing";

        return "paused";
    }

    @Override
    public boolean getInline() {
        return isInline;
    }

    @Override
    public long getPosition() {
        if (player == null) return -1L;

        return player.getCurrentPosition();
    }
}
