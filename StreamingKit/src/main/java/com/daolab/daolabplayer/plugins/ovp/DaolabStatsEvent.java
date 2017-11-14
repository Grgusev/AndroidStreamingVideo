

package com.daolab.daolabplayer.plugins.ovp;

import com.daolab.daolabplayer.PKEvent;

/**
 * Created by almond on 27/03/2017.
 */

public class DaolabStatsEvent implements PKEvent{

    public enum Type {
        REPORT_SENT
    }

    public static class DaolabStatsReport extends DaolabStatsEvent {

        public final String reportedEventName;

        public DaolabStatsReport(String reportedEventName) {
            this.reportedEventName = reportedEventName;
        }
    }


    @Override
    public Enum eventType() {
        return Type.REPORT_SENT;
    }
}
