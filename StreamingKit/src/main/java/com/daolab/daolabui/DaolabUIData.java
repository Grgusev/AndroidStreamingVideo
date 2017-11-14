package com.daolab.daolabui;

/**
 * Created by almond on 8/5/2017.
 */

public class DaolabUIData {
    public static int MSG_CONTROL_VIEW  = 1001;
    public static int SHOW_CONTROL_TIME = 4000;

    public static DaolabPlayerView FULLSCREEN_PLAYER = null;
    public static int PLAYING_STATUS = -1;
    public static String FULLSCREEN_STATUS = "playing";

    public static int SCREEN_ORIENTATION_LANDSCAPE = 0;
    public static int SCREEN_ORIENTATION_AUTO = 1;
    public static int SCREEN_ORIENTATION_PORTRAIT = 2;

    public static String DaoLabPlayerID = "IDENTIFY";
    public static String DaoLabPlayEventTypePlay = "DID_PLAY";
    public static String DaoLabPlayEventTypePause = "DID_PAUSE";

    public static String DaoLabPlayEventTypeSeekPosition = "DID_SEEK_TO_POSITION";
    public static String DaoLabPlayEventTypeSeekTo = "DID_SEEK_TO";
    public static String DaoLabPlayEventTypeFromTime = "fromTime";
    public static String DaoLabPlayEventTypeToTime = "toTime";

    public static String DaoLabPlayEventTypePlayStart = "DID_START_PLAY";
    public static String DaoLabPlayEventTypeSeekToPlay = "SEEK_TO_PLAY";

    public static String DaoLabAudioEventTypeMute = "DID_MUTE";
    public static String DaoLabAudioEventTypeUnMute = "DID_UNMUTE";

    public static String DaoLabPlayerResolutionId = "RESOLUTION_IDENTIFY";
    public static String DaoLabPlayEventTypeSelectResolution = "SELECT_RESOLUTION";
    public static String DaoLabPlayEventTypeDidResolution = "DID_RESOLUTION";

    public static String DaoLabPlayerSubtitleId = "SUBTITLE_IDENTIFY";
    public static String DaoLabPlayEventTypeSelectSubtitle = "SELECT_SUBTITLE";
    public static String DaoLabPlayEventTypeDidSubtitle = "DID_SUBTITLE";

    public static String DaoLabScreenEventTypeExitFullScreen = "EXIT_FULLSCREEN";
    public static String DaoLabScreenEventTypeEnterFullScreen = "ENTER_FULLSCREEN";
    public static String DaoLabScreenEventTypePortrait = "PORTRAIT,";
    public static String DaoLabScreenEventTypeLandscape = "LANDSCAPE";

    public static String SET_MUTE   = "daolab_set_mute";
    public static String SET_UNMUTE   = "daolab_set_unmute";
}
