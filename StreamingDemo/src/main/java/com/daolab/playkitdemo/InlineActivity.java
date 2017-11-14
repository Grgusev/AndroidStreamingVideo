package com.daolab.playkitdemo;

import android.Manifest;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.Toast;

import com.daolab.daolabui.DaolabPlayerView;

public class InlineActivity extends AppCompatActivity {

    DaolabPlayerView playerView, inlinePlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_full_screen);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_DENIED) {
            Toast.makeText(this, "Please tap ALLOW", Toast.LENGTH_LONG).show();
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                    1);
        }

        playerView = (DaolabPlayerView)this.findViewById(R.id.playerView);
        playerView.setActivity(this);

        playerView.setHLSURL("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8", "", "");

        playerView.startPlay();

        inlinePlayer = (DaolabPlayerView)this.findViewById(R.id.inlinePlayerView);
        inlinePlayer.setActivity(this);

        inlinePlayer.setHLSURL("https://bitdash-a.akamaihd.net/content/sintel/hls/playlist.m3u8", "", "");

        inlinePlayer.startPlay();
    }

    @Override
    protected void onPause() {
        super.onPause();
        playerView.onPause();
        inlinePlayer.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        playerView.onResume();
        inlinePlayer.onResume();
    }
}
