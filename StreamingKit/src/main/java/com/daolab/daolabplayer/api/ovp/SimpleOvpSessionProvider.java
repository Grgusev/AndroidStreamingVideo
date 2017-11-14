

package com.daolab.daolabplayer.api.ovp;

import android.text.TextUtils;

import com.daolab.daolabplayer.mediaproviders.ovp.DaolabOvpMediaProvider;
import com.daolab.netkit.connect.response.PrimitiveResult;
import com.daolab.netkit.utils.OnCompletion;
import com.daolab.netkit.utils.SessionProvider;

/**
 * A SessionProvider that just reflects its input parameters -- baseUrl, partnerId, ks.
 * This class does not attempt to manage a session. The application is expected to provide a valid KS, which it
 * can update as required by calling {@link #setKs(String)}.
 * For some use cases, the KS can be null (anonymous media playback, if allowed by access-control).
 * <p>
 * Basic usage with a {@link DaolabOvpMediaProvider}:
 * <pre>
 * {@code
 *      new DaolabOvpMediaProvider()
 *          .setSessionProvider(new SimpleOvpSessionProvider("https://, 1851571, null))
 *          .setEntryId("0_pl5lbfo0")
 *          .load(completion);
 * }
 * </pre>
 * </p>
 */
public class SimpleOvpSessionProvider implements SessionProvider {

    private String baseUrl;
    private int partnerId;
    private String ks;

    /**
     * Build an OVP {@link SessionProvider} with the specified parameters.
     * 
     * @param baseUrl       Daolab Server URL, such as "https://".
     * @param partnerId     Daolab partner id.
     * @param ks            Daolab Session token.
     */
    public SimpleOvpSessionProvider(String baseUrl, int partnerId, String ks) {
        // Ensure baseUrl, partnerId are not empty.
        if (TextUtils.isEmpty(baseUrl)) {
            throw new IllegalArgumentException("Missing baseUrl");
        }
        if (partnerId == 0) {
            throw new IllegalArgumentException("Missing partnerId");
        }

        this.baseUrl = baseUrl;
        this.partnerId = partnerId;
        this.ks = ks;
    }

    /**
     * Update the session token.
     * @param ks            Valid Daolab Session token.
     */
    public void setKs(String ks) {
        this.ks = ks;
    }

    @Override
    public String baseUrl() {
        return baseUrl;
    }

    @Override
    public void getSessionToken(OnCompletion<PrimitiveResult> completion) {
        completion.onComplete(new PrimitiveResult(ks));
    }

    @Override
    public int partnerId() {
        return partnerId;
    }
}
