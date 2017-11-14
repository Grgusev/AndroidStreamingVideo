package com.daolab.playkitdemo;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

public class RecycleVideoActivity extends AppCompatActivity {

    RecyclerView videoList;
    VideoAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_recycle_video);
        videoList = (RecyclerView)this.findViewById(R.id.recycler_view);
        videoList.setLayoutManager(new LinearLayoutManager(this));
        adapter = new VideoAdapter(this);
        videoList.setAdapter(adapter);
    }

    @Override
    protected void onResume() {
        super.onResume();
        adapter.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        adapter.onPause();
    }
}
