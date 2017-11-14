package com.daolab.daolabui;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.daolab.daolabplayer.R;


public class TrackItemAdapter extends RecyclerView.Adapter<TrackItemAdapter.CustomViewHolder> {

    private Context context;
    private TrackItem[] trackItems;
    private int selectedPos     = -1;

    public interface OnItemClickListener
    {
        void onItemClick(int position);
    }

    OnItemClickListener trackListener = null;

    public TrackItemAdapter(Context context, TrackItem[] trackItems) {
        this.context = context;
        this.trackItems = trackItems;
    }

    @Override
    public CustomViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_track, null);
        CustomViewHolder viewHolder = new CustomViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(final CustomViewHolder holder, final int position) {
        holder.track_name.setText(trackItems[position].getTrackName());
        holder.setDisable();

        if (selectedPos != -1 && position == selectedPos)
        {
            holder.setEnable();
        }

        holder.track_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectedPos = position;
                holder.setEnable();
                notifyDataSetChanged();

                if (trackListener != null)
                {
                    trackListener.onItemClick(position);
                }
            }
        });
    }

    public void setOnItemClickListener(OnItemClickListener listener)
    {
        trackListener = listener;
    }

    public void setSelectedPos(int position)
    {
        selectedPos = position;
        notifyDataSetChanged();
    }

    @Override
    public int getItemCount() {
        return trackItems.length;
    }

    class CustomViewHolder extends RecyclerView.ViewHolder {
        TextView track_name;
        LinearLayout select_area;

        public CustomViewHolder(View view) {
            super(view);
            track_name = (TextView)view.findViewById(R.id.track_name);
            select_area = (LinearLayout)view.findViewById(R.id.select_cursor);
        }

        public void setDisable()
        {
            track_name.setTextColor(Color.parseColor("#AAAAAA"));
            select_area.setVisibility(View.INVISIBLE);
        }

        public void setEnable()
        {
            track_name.setTextColor(Color.parseColor("#FFFFFF"));
            select_area.setVisibility(View.VISIBLE);
        }
    }
}
