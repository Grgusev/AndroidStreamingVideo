package com.daolab.daolabui;

import android.content.Intent;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;

import com.daolab.daolabplayer.R;

public class DaolabPlayerActivity extends AppCompatActivity {

    DaolabPlayerView player;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActionBar actionBar = this.getSupportActionBar();

        if (actionBar != null) actionBar.hide();

        setContentView(R.layout.activity_daolab_player);

        player = (DaolabPlayerView) this.findViewById(R.id.playerView);

        Intent intent = this.getIntent();

        if (intent == null) finish();

        String url = intent.getStringExtra("url");

        if (TextUtils.isEmpty(url)) finish();

        player.setActivity(this);

        player.setHLSURL(url, "", "");

        player.setFullscreenActivity(true);
        player.setInline(false);

        player.startPlay();

    }

    @Override
    protected void onPause() {
        super.onPause();
        player.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        player.onResume();
    }
}
