package com.daolab.playkitdemo;

import android.app.Activity;
import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.daolab.daolabmedia.ConfigManager;
import com.daolab.daolabui.DaolabPlayer;
import com.daolab.daolabui.DaolabPlayerView;
import com.daolab.daolabui.DaolabVideoPlayerEventListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Map;

/**
 * Created by almond on 8/7/2017.
 */

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.CustomViewHolder> {

    Activity mActivity;
    ArrayList<CustomViewHolder> holderList = new ArrayList<>();
    Context context;
    String videoIDs[] = new String[6];

    public VideoAdapter(Activity activity)
    {
        mActivity = activity;
        context = activity.getApplicationContext();

        videoIDs[0] = "L0-1505928584-3c9f6fcd-9de6-11e7-938a-334008387590";
        videoIDs[1] = "L0-038f44d6-9310-11e7-a61d-c3c02e914257";
        videoIDs[2] = "L0-2d962843-930e-11e7-932c-1b4e9f7ad685";
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_row, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);

        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        ConfigManager.setIdentifier("ABCDEFGHIJKLMNOPQRSTUVWXYZ123456");
        ConfigManager.findVideoWithVideoID(context, videoIDs[position % 3], new ConfigManager.mediaCallbackInterface() {
            @Override
            public void onLoadedVideoID(JSONObject jsonResult) {
                try {
                    if (holder.isValueSet == false)
                    {
                        if (jsonResult.has("ad")) {
                            String adUrl = jsonResult.getString("ad");
                            if (!TextUtils.isEmpty(adUrl)) {
                                holder.player.setGoogleAdLink(adUrl);
                            }
                        }

                        holder.setHLSUrlPlay(jsonResult.getString("url"));
                        holder.player.setThumbnailUrl(jsonResult.getString("img"));

                    }
                    else
                    {
                        holder.resume();
                    }
                } catch(JSONException je) {
                    android.util.Log.e("VideoAdapter", "Invalid JSON");
                }
            }
        });

        holderList.add(holder);
    }

    @Override
    public void onViewRecycled(CustomViewHolder holder) {
        super.onViewRecycled(holder);
        holder.pause();
        holderList.remove(holder);
    }

    public void onResume()
    {
        for (int i = 0; i < holderList.size(); i ++)
        {
            holderList.get(i).resume();
        }
    }

    public void onPause()
    {
        for (int i = 0; i < holderList.size(); i ++)
        {
            holderList.get(i).pause();
        }
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public int getItemViewType(int position) {
        return position;
    }

    @Override
    public int getItemCount() {
        return 30;
    }

    static boolean first = true;

    class CustomViewHolder extends RecyclerView.ViewHolder {
        protected DaolabPlayer player;
        public boolean isValueSet   = false;

        public CustomViewHolder(View view) {
            super(view);
            player = (DaolabPlayerView) view.findViewById(R.id.player);
            player.setActivity(mActivity);
            player.setAutoPlayResume(false);
            if(first) {
                player.setVideoPlayerEventListener(new EventListener());
                first = false;
            }
        }

        public void setHLSUrlPlay(String url)
        {
            player.setHLSURL(url, "", "");
            isValueSet = true;
        }

        public void resume()
        {
            player.onResume();
        }

        public void pause()
        {
            player.onPause();
        }
    }

    class EventListener implements DaolabVideoPlayerEventListener {
        public void onPlaybackChange(String name, Map<String, Object> attributes) {

        }

        public void onScreenChange(String name, Map<String, Object> attributes) {
            android.util.Log.i("Event Listener", name);
            android.util.Log.i("Event Listener", attributes.toString());
        }

        @Override
        public void onAudioChange(String name, Map<String, Object> attributes) {

        }

        @Override
        public void onSubtitleChange(String name, Map<String, Object> mParams) {

        }

        @Override
        public void onQualityChange(String name, Map<String, Object> mParams) {

        }
    }
}
