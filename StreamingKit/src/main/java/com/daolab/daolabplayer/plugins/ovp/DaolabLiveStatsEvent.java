

package com.daolab.daolabplayer.plugins.ovp;

import com.daolab.daolabplayer.PKEvent;

/**
 * Created by almond on 27/03/2017.
 */

public class DaolabLiveStatsEvent implements PKEvent {

    public enum Type {
        REPORT_SENT
    }

    public static class DaolabLiveStatsReport extends DaolabLiveStatsEvent {

        public final long bufferTime;

        public DaolabLiveStatsReport(long bufferTime) {
            this.bufferTime = bufferTime;
        }

    }


    @Override
    public Enum eventType() {
        return Type.REPORT_SENT;
    }
}
