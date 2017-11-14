

package com.daolab.daolabplayer;

public interface PKEvent {
    Enum eventType();

    interface Listener {
        void onEvent(PKEvent event);
    }
}

