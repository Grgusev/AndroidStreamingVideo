

package com.daolab.daolabplayer;

import com.daolab.daolabplayer.mediaproviders.base.OnMediaLoadCompletion;

public interface MediaEntryProvider {

    void load(OnMediaLoadCompletion completion);

    void cancel();
}
