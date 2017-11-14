package com.daolab.daolabui;

import android.content.Intent;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ImageButton;

import com.daolab.daolabplayer.R;

public class VideoFullScreen extends AppCompatActivity{

    DaolabPlayerView player;
    ViewGroup orgParent;
    ViewGroup.LayoutParams params;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);

//        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) actionBar.hide();

        setContentView(R.layout.activity_video_full_screen);

        Intent intent = this.getIntent();

        if (intent == null) finish();

        player = DaolabUIData.FULLSCREEN_PLAYER;

        orgParent = (ViewGroup)player.getParent();
        params = player.getLayoutParams();

        orgParent.removeView(player);

        player.setLayoutParams(new ConstraintLayout.LayoutParams(-1, -1));
        ((ConstraintLayout)this.findViewById(R.id.parent)).addView(player);

        player.setFullScreen(true);

        player.setActivity(this);
        player.showBackIcon(true);
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null)
        {
            if (player.nowPlaying == true) DaolabUIData.PLAYING_STATUS = 1;
            else DaolabUIData.PLAYING_STATUS = 0;
            player.onPause();
            player.nowPlaying = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (player != null) {
            player.setFullScreen(false);
            ((ViewGroup)player.getParent()).removeView(player);
            orgParent.addView(player, params);

            player.setExitFullScreenTrigger();
            player.exitFullScreenPlaying();
            player.showBackIcon(false);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null)
        {
            player.onResume();

            boolean playing = getIntent().getBooleanExtra("playing", true);
            player.nowPlaying = false;

            if (playing) player.resumePlay();
        }
    }

    @Override
    public void finish() {
        overridePendingTransition(0, 0);
        super.finish();
    }
}
