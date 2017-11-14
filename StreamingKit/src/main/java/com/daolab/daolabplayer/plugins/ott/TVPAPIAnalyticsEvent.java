

package com.daolab.daolabplayer.plugins.ott;

import com.daolab.daolabplayer.PKEvent;

/**
 * Created by almond on 27/03/2017.
 */

public class TVPAPIAnalyticsEvent implements PKEvent {

    public enum Type {
        REPORT_SENT
    }

    public static class TVPAPIAnalyticsReport extends TVPAPIAnalyticsEvent {

        public final String reportedEventName;

        public TVPAPIAnalyticsReport(String reportedEventName) {
            this.reportedEventName = reportedEventName;
        }
    }


    @Override
    public Enum eventType() {
        return Type.REPORT_SENT;
    }
}
