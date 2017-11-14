package com.daolab.daolabui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.media.AudioManager;
import android.os.Handler;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import com.daolab.daolabplayer.PKEvent;
import com.daolab.daolabplayer.PlayerEvent;
import com.google.android.exoplayer2.C;
import com.daolab.daolabplayer.PKLog;
import com.daolab.daolabplayer.Player;
import com.daolab.daolabplayer.PlayerState;

import java.util.Formatter;
import java.util.HashMap;
import java.util.Locale;

import com.daolab.daolabplayer.R;

/**
 * Created by almond on 07/11/2016.
 */

public class PlaybackControlsView extends RelativeLayout implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {

    private static final PKLog log = PKLog.get("PlaybackControlsView");
    private static final int PROGRESS_BAR_MAX = 100;

    Context mContext;
    private Player player;
    private PlayerState playerState;
    DaolabPlayerView viewer = null;

    private Formatter formatter;
    private StringBuilder formatBuilder;

    private SeekBar seekBar;
    private TextView tvCurTime, tvTime;
    private ImageButton btnPlay, btnFullScreen;
    private ImageButton btnVolume, btnOrientation;
    private ImageButton btnBack;
    private ImageButton adBtnVolume;

    private LinearLayout mController;
    private LinearLayout mVideoTrackSection;
    private LinearLayout mSubtitleTrackSection;
    private TextView mVideoTrackSpinner;
    private ImageView mSubtitleTrackSpinner;
    private LinearLayout playing_control;

    RecyclerView mVideoTrackList;
    RecyclerView mSubtitleTrackList;

    static private boolean isMute = false;
    static private int orgDevVolume = 0;

    private boolean dragging = false;
    private boolean visible = false;

    static private float mPrevVolume = -1f;
    private boolean isActive    = false;

    private int isScreenOrientation = DaolabUIData.SCREEN_ORIENTATION_AUTO;

    private Runnable updateProgressAction = new Runnable() {
        @Override
        public void run() {
            updateProgress();
        }
    };

    public PlaybackControlsView(Context context) {
        this(context, null);
    }

    public PlaybackControlsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PlaybackControlsView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        mContext = context;
        LayoutInflater.from(context).inflate(R.layout.playback_layout, this);
        formatBuilder = new StringBuilder();
        formatter = new Formatter(formatBuilder, Locale.getDefault());
        initPlaybackControls();
    }

    private BroadcastReceiver mMuteReceiver  = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(DaolabUIData.SET_MUTE))
            {
                player.setVolume(0.0f);
                setVolumeImageResource(0.0f);
                isMute = true;
            }
            else
            {
                player.setVolume(mPrevVolume);
                setVolumeImageResource(mPrevVolume);
                isMute = false;
            }
        }
    };

    public void setViewer(DaolabPlayerView view)
    {
        viewer = view;
        setOrientationImageResource(DaolabUIData.SCREEN_ORIENTATION_AUTO);
    }

    private void initPlaybackControls() {

        btnPlay = (ImageButton) this.findViewById(R.id.playPause);
        btnFullScreen = (ImageButton)this.findViewById(R.id.fullscreen);
        btnVolume = (ImageButton)this.findViewById(R.id.volumeCtrl);
        adBtnVolume = (ImageButton)this.findViewById(R.id.adVolumeCtrl);
        btnOrientation = (ImageButton)this.findViewById(R.id.orientCtrl);
        btnBack = (ImageButton)this.findViewById(R.id.back_button);
        btnBack.setVisibility(View.GONE);

        btnPlay.setOnClickListener(this);
        btnFullScreen.setOnClickListener(this);
        btnVolume.setOnClickListener(this);
        adBtnVolume.setOnClickListener(this);
        btnOrientation.setOnClickListener(this);
        btnBack.setOnClickListener(this);

        seekBar = (SeekBar) this.findViewById(R.id.mediacontroller_progress);
        seekBar.setOnSeekBarChangeListener(this);

        tvCurTime = (TextView) this.findViewById(R.id.time_current);
        tvTime = (TextView) this.findViewById(R.id.time);

        mController = (LinearLayout) this.findViewById(R.id.player_control);
        mVideoTrackSection = (LinearLayout)this.findViewById(R.id.video_part);
        mSubtitleTrackSection = (LinearLayout)this.findViewById(R.id.subtitle_part);
        mVideoTrackSpinner = (TextView) this.findViewById(R.id.video_select);
        mSubtitleTrackSpinner = (ImageView) this.findViewById(R.id.subtitle_select);
        playing_control = (LinearLayout)this.findViewById(R.id.playing_control);

        mVideoTrackSpinner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.setVisibility(View.GONE);
                mVideoTrackSection.setVisibility(View.VISIBLE);

                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, viewer);

                viewer.callQualityChange(DaolabUIData.DaoLabPlayEventTypeSelectResolution, mParams);

                viewer.showControlView(true);
            }
        });

        mSubtitleTrackSpinner.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mController.setVisibility(View.GONE);
                mSubtitleTrackSection.setVisibility(View.VISIBLE);

                HashMap<String, Object> mParams = new HashMap<String, Object>();
                mParams.put(DaolabUIData.DaoLabPlayerID, viewer);

                viewer.callSubtitleChange(DaolabUIData.DaoLabPlayEventTypeSelectSubtitle, mParams);

                viewer.showControlView(true);
            }
        });

        this.findViewById(R.id.video_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mVideoTrackSection.setVisibility(View.GONE);
                mController.setVisibility(View.VISIBLE);

                viewer.showControlView(true);
            }
        });

        this.findViewById(R.id.subtitle_close).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                mSubtitleTrackSection.setVisibility(View.GONE);
                mController.setVisibility(View.VISIBLE);

                viewer.showControlView(true);
            }
        });

        mVideoTrackList = (RecyclerView) this.findViewById(R.id.video_track_list);
        mSubtitleTrackList = (RecyclerView) this.findViewById(R.id.subtitle_track_list);

        LinearLayoutManager layoutManager
                = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);

        LinearLayoutManager layoutManager1
                = new LinearLayoutManager(mContext, LinearLayoutManager.HORIZONTAL, false);

        mVideoTrackList.setLayoutManager(layoutManager);
        mSubtitleTrackList.setLayoutManager(layoutManager1);

        if (mSettingsContentObserver == null)
        {
            mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
            mContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );
        }

        registerMuteReceiver(true);
    }

    public void showAdControlView(boolean visible)
    {
        if (visible)
        {
            playing_control.setVisibility(View.GONE);
            adBtnVolume.setVisibility(View.VISIBLE);
        }
        else
        {
            playing_control.setVisibility(View.VISIBLE);
            adBtnVolume.setVisibility(View.GONE);
        }
    }

    public void registerMuteReceiver(boolean regist)
    {
        if (regist)
        {
            IntentFilter filter = new IntentFilter(DaolabUIData.SET_MUTE);
            filter.addAction(DaolabUIData.SET_UNMUTE);
            LocalBroadcastManager.getInstance(mContext).registerReceiver(mMuteReceiver, filter);
        }
        else
        {
            LocalBroadcastManager.getInstance(mContext).unregisterReceiver(mMuteReceiver);
        }
    }

    public void setMuteIntent(boolean mute)
    {
        if (mute)
        {
            Intent intent = new Intent();
            intent.setAction(DaolabUIData.SET_MUTE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            HashMap<String, Object> mParams = new HashMap<String, Object>();
            mParams.put(DaolabUIData.DaoLabPlayerID, viewer);

            viewer.callAudioEventListener(DaolabUIData.DaoLabAudioEventTypeMute, mParams);
        }
        else
        {
            Intent intent = new Intent();
            intent.setAction(DaolabUIData.SET_UNMUTE);
            LocalBroadcastManager.getInstance(mContext).sendBroadcast(intent);

            HashMap<String, Object> mParams = new HashMap<String, Object>();
            mParams.put(DaolabUIData.DaoLabPlayerID, viewer);

            viewer.callAudioEventListener(DaolabUIData.DaoLabAudioEventTypeUnMute, mParams);
        }
    }

    public void setViewOnThumbnail()
    {
        mVideoTrackList.setVisibility(View.INVISIBLE);
        this.findViewById(R.id.progress).setVisibility(View.INVISIBLE);
        this.setVisibility(View.VISIBLE);
        btnPlay.setBackgroundResource(R.drawable.btn_play);
    }

    public void setViewNotThumbnail()
    {
        mVideoTrackList.setVisibility(View.VISIBLE);
        this.findViewById(R.id.progress).setVisibility(View.VISIBLE);
        this.setVisibility(View.VISIBLE);
    }

    public void setTrackSectionVisibility(int visible)
    {
        this.findViewById(R.id.track_list).setVisibility(visible);
    }

    public void setControllerVisibility(int visible)
    {
        mController.setVisibility(visible);
    }

    public void setVideoSectionVisibility(int visible)
    {
        mVideoTrackSection.setVisibility(visible);
    }

    public void setSubtitleSectionVisibility(int visible)
    {
        mSubtitleTrackSection.setVisibility(visible);
    }

    public void setVideoTrackVisibility(int visible)
    {
        mVideoTrackSpinner.setVisibility(visible);
    }

    public void setSubtitleTrackVisibility(int visible)
    {
        mSubtitleTrackSpinner.setVisibility(visible);
    }

    public void setVideoTrackAdapter(TrackItemAdapter adapter)
    {
        mVideoTrackList.setAdapter(adapter);
    }

    public void setSubtitleTrackAdapter(TrackItemAdapter adapter)
    {
        mSubtitleTrackList.setAdapter(adapter);
    }

    private void setOrientationImageResource(int orientation)
    {
        isScreenOrientation = orientation;
        if (orientation == DaolabUIData.SCREEN_ORIENTATION_LANDSCAPE)
        {
            btnOrientation.setBackgroundResource(R.drawable.landscape);
        }
        else
        {
            btnOrientation.setBackgroundResource(R.drawable.auto_rotation);
        }

        viewer.setScreenOrientation(orientation);
    }

    public void setVolumeImageResource(float volume)
    {
        if (volume == 0)
        {
            btnVolume.setBackgroundResource(R.drawable.ico_volume_off);
            adBtnVolume.setBackgroundResource(R.drawable.ico_volume_ad_off);
        }
        else if (volume < 1f / 3)
        {
            btnVolume.setBackgroundResource(R.drawable.ico_volume_on);
            adBtnVolume.setBackgroundResource(R.drawable.ico_volume_ad_on);
        }
        else if (volume < 2f / 3)
        {
            btnVolume.setBackgroundResource(R.drawable.ico_volume_on);
            adBtnVolume.setBackgroundResource(R.drawable.ico_volume_ad_on);
        }
        else
        {
            btnVolume.setBackgroundResource(R.drawable.ico_volume_on);
            adBtnVolume.setBackgroundResource(R.drawable.ico_volume_ad_on);
        }
    }

    public void setVideoTrackText(String videoTrackText) {
        mVideoTrackSpinner.setText(videoTrackText);
    }

    public class SettingsContentObserver extends ContentObserver {
        private AudioManager audioManager;

        public SettingsContentObserver(Context context, Handler handler) {
            super(handler);
            audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
        }

        @Override
        public boolean deliverSelfNotifications() {
            return false;
        }

        @Override
        public void onChange(boolean selfChange) {
            int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
            int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
            if (player != null && isMute == false)
            {
                player.setVolume(currentVolume * 1.0f / maxVolume);
            }
        }
    }

    SettingsContentObserver mSettingsContentObserver;

    private void initVolumeChangeListener()
    {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        int currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);

        if (isMute)
        {
            player.setVolume(0.0f);
            setVolumeImageResource(0.0f);
        }
        else if (currentVolume != 0)
        {
            mPrevVolume = currentVolume * 1.0f / maxVolume;
            player.setVolume(mPrevVolume);
            setVolumeImageResource(mPrevVolume);
            isMute = false;
        }
        else if (currentVolume == 0)
        {
            mPrevVolume = 1.0f;
            player.setVolume(0.0f);
            setVolumeImageResource(0.0f);
            isMute = true;
        }


        player.addEventListener(new PKEvent.Listener() {
            @Override
            public void onEvent(PKEvent event) {
                //When the track data available, this event occurs. It brings the info object with it.
                PlayerEvent.VolumeChanged volumeEvent = (PlayerEvent.VolumeChanged) event;

                if (volumeEvent.volume != 0) {
                    mPrevVolume = volumeEvent.volume;
                    isMute = false;
                }
                else if (volumeEvent.volume == 0)
                {
                    isMute = true;
                }

                setVolumeImageResource(volumeEvent.volume);
            }
        }, PlayerEvent.Type.VOLUME_CHANGED);
    }

    public void setVisibleOrientation(boolean visible)
    {
        if (visible)
        {
            btnOrientation.setVisibility(View.VISIBLE);
        }
        else
        {
            btnOrientation.setVisibility(View.GONE);
        }
    }

    public void setLive(boolean isLive)
    {
        if (isLive)
        {
            this.findViewById(R.id.time_section).setVisibility(View.GONE);
            this.findViewById(R.id.live_section).setVisibility(View.VISIBLE);
        }
        else
        {
            this.findViewById(R.id.time_section).setVisibility(View.VISIBLE);
            this.findViewById(R.id.live_section).setVisibility(View.GONE);
        }
    }

    private void updateProgress() {
        long duration = C.TIME_UNSET;
        long position = C.POSITION_UNSET;
        long bufferedPosition = 0;
        if(player != null){
            duration = player.getDuration();
            position = player.getCurrentPosition();
            bufferedPosition = player.getBufferedPosition();
        }

        if(duration != C.TIME_UNSET){
            log.d("updateProgress Set Duration:" + duration);
            tvTime.setText(stringForTime(duration));
        }

        if (!dragging && position != C.POSITION_UNSET && duration != C.TIME_UNSET) {
            log.d("updateProgress Set Position:" + position);
            tvCurTime.setText(stringForTime(position));
            seekBar.setProgress(progressBarValue(position));
        }

        seekBar.setSecondaryProgress(progressBarValue(bufferedPosition));
        // Remove scheduled updates.
        removeCallbacks(updateProgressAction);
        // Schedule an update if necessary.

        if (playerState != PlayerState.IDLE && position < duration) {
            long delayMs = 1000;
            postDelayed(updateProgressAction, delayMs);
        }

        if (duration != C.TIME_UNSET && position > duration)
        {
            player.seekTo(0);
            player.pause();
            btnPlay.setBackgroundResource(R.drawable.btn_play);
            viewer.showThumbnail(true);
        }
    }

    public void setPlayButtonVisible(int visible)
    {
        btnPlay.setVisibility(visible);
    }

    private int progressBarValue(long position) {
        int progressValue = 0;
        if(player != null){
            long duration = player.getDuration();
            if (duration > 0) {
                progressValue = (int) ((position * PROGRESS_BAR_MAX) / duration);
            }
        }

        return progressValue;
    }

    private long positionValue(int progress) {
        long positionValue = 0;
        if(player != null){
            long duration = player.getDuration();
            positionValue = (duration * progress) / PROGRESS_BAR_MAX;
        }

        return positionValue;
    }

    private String stringForTime(long timeMs) {

        long totalSeconds = (timeMs + 500) / 1000;
        long seconds = totalSeconds % 60;
        long minutes = (totalSeconds / 60) % 60;
        long hours = totalSeconds / 3600;
        formatBuilder.setLength(0);
        return hours > 0 ? formatter.format("%d:%02d:%02d", hours, minutes, seconds).toString()
                : formatter.format("%02d:%02d", minutes, seconds).toString();
    }

    public void setPlayer(Player player) {
        this.player = player;
    }

    public void setPlayerState(PlayerState playerState) {
        this.playerState = playerState;
        updateProgress();
    }

    public void setPlayingState(PlayerEvent.Type type) {
        if (type == PlayerEvent.Type.PAUSE)
        {
            btnPlay.setBackgroundResource(R.drawable.btn_play);
        }

        if (type == PlayerEvent.Type.PLAY) {
            btnPlay.setBackgroundResource(R.drawable.btn_pause);
        }
    }

    public void setPlayerActive()
    {
        if (isActive == false)
        {
            initVolumeChangeListener();
            isActive    = true;
        }
    }

    public void setFullscreenImage(boolean fullscreen)
    {
        if (fullscreen)
        {
            btnFullScreen.setBackgroundResource(R.drawable.ico_fullscreen);
        }
        else
        {
            btnFullScreen.setBackgroundResource(R.drawable.ico_player_enlarge);
        }
    }

    private int getDeviceVolume()
    {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        return audioManager.getStreamVolume(AudioManager.STREAM_MUSIC);
    }

    private void setDeviceDefaultVolume()
    {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);
        int maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, maxVolume / 10, AudioManager.FLAG_PLAY_SOUND);
    }

    private void setDeviceDefaultVolume(int volume)
    {
        AudioManager audioManager = (AudioManager) mContext.getSystemService(Context.AUDIO_SERVICE);

        audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, volume, AudioManager.FLAG_PLAY_SOUND);
    }

    public void setBackupDeviceDefaultVolume()
    {
        setDeviceDefaultVolume(orgDevVolume);
    }

    public void getBackupDeviceDefaultVolume()
    {
        orgDevVolume = getDeviceVolume();

        if (isMute)
        {
            setDeviceDefaultVolume(0);
            setVolumeImageResource(0);
        }
    }

    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.playPause) {
            if (player != null) {
                if (viewer.nowPlaying) {
                    viewer.pausePlay();
                } else {
                    if (viewer.isPrepared)
                    {
                        player.play();
                        viewer.showThumbnail(false);
                    }
                    else
                    {
                        viewer.playStart();
                    }

                    HashMap<String, Object> mParams = new HashMap<String, Object>();
                    mParams.put(DaolabUIData.DaoLabPlayerID, viewer);

                    viewer.callPlayEventListener(DaolabUIData.DaoLabPlayEventTypePlay, mParams);
                }
            }

        } else if (i == R.id.fullscreen) {
            viewer.toggleFullscreen();
        } else if (i == R.id.volumeCtrl)
        {
            if (player.getVolume() == 0f || isMute)
            {
                if (getDeviceVolume() == 0)
                {
                    setDeviceDefaultVolume();
                    mPrevVolume = 1.0f;
                }
                player.setVolume(mPrevVolume);
                setVolumeImageResource(mPrevVolume);
                isMute = false;
                setMuteIntent(false);
            }
            else
            {
                player.setVolume(0f);
                setVolumeImageResource(0f);
                isMute = true;
                setMuteIntent(true);
            }
        }
        else if (i == R.id.adVolumeCtrl)
        {
            if (player.getVolume() == 0f || isMute)
            {
                if (orgDevVolume == 0)
                {
                    setDeviceDefaultVolume();
                    mPrevVolume = 1.0f;
                }
                else
                {
                    setDeviceDefaultVolume(orgDevVolume);
                    mPrevVolume = 1.0f;
                }

                player.setVolume(mPrevVolume);
                setVolumeImageResource(mPrevVolume);
                isMute = false;
                setMuteIntent(false);
            }
            else
            {
                android.util.Log.e("almond", "volume mute");
                player.setVolume(0f);
                setDeviceDefaultVolume(0);
                setVolumeImageResource(0f);
                isMute = true;
                setMuteIntent(true);
            }
        }
        else if (i == R.id.orientCtrl)
        {
            if (isScreenOrientation == DaolabUIData.SCREEN_ORIENTATION_AUTO)
            {
                setOrientationImageResource(DaolabUIData.SCREEN_ORIENTATION_LANDSCAPE);
            }
            else
            {
                setOrientationImageResource(DaolabUIData.SCREEN_ORIENTATION_AUTO);
            }
        } else if (i == R.id.back_button)
        {
            viewer.toggleFullscreen();
        }
    }

    public void showBackIcon(boolean visible)
    {
        if (visible)
        {
            btnBack.setVisibility(View.VISIBLE);
        }
        else
        {
            btnBack.setVisibility(View.GONE);
        }
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (fromUser) {
            tvCurTime.setText(stringForTime(positionValue(progress)));
        }
    }


    public void onStartTrackingTouch(SeekBar seekBar) {
        dragging = true;
        viewer.setFromTime(stringForTime(player.getCurrentPosition()));
    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {
        dragging = false;

        player.seekTo(positionValue(seekBar.getProgress()));
    }

    public void release() {
        removeCallbacks(updateProgressAction);
        if (mSettingsContentObserver != null) {
            mContext.getContentResolver().unregisterContentObserver(mSettingsContentObserver);
            mSettingsContentObserver = null;
        }
        registerMuteReceiver(false);
    }

    public void resume() {
        updateProgress();
        if (mSettingsContentObserver == null)
        {
            mSettingsContentObserver = new SettingsContentObserver(mContext, new Handler());
            mContext.getContentResolver().registerContentObserver(android.provider.Settings.System.CONTENT_URI, true, mSettingsContentObserver );
        }

        registerMuteReceiver(true);

        if (mPrevVolume != -1f && isMute == false)
        {
            player.setVolume(mPrevVolume);
            setVolumeImageResource(mPrevVolume);
        }

        if (isMute)
        {
            player.setVolume(0.0f);
            setVolumeImageResource(0.0f);
        }
        viewer.showControlView(true);

    }

    public void setInline(boolean inline) {
        if (inline == true)
        {
            btnOrientation.setVisibility(View.GONE);
        }
        else
        {
            btnOrientation.setVisibility(View.GONE);
//            btnOrientation.setVisibility(View.VISIBLE);
        }
    }

    public void showVisible(boolean visibility) {
        visible = visibility;
    }

    public boolean getVisible()
    {
        return visible;
    }
}
