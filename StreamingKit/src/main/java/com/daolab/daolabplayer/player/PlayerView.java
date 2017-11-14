

package com.daolab.daolabplayer.player;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.FrameLayout;

/**
 * Created by almond on almond.
 */

public abstract class PlayerView extends FrameLayout {
    public PlayerView(Context context) {
        super(context);
    }

    public PlayerView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public PlayerView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }


    public abstract void hideVideoSurface();
    public abstract void showVideoSurface();
    public abstract void hideVideoSubtitles();
    public abstract void showVideoSubtitles();

}
