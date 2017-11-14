package com.daolab.daolabmedia;

import com.google.android.exoplayer2.upstream.*;
import com.google.android.exoplayer2.upstream.HttpDataSource.BaseFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource.Factory;

/**
 * Created by carlos on 30/8/2017.
 */

/** A {@link Factory} that produces {@link DefaultHttpDataSource} instances. */
public final class DaolabHttpDataSourceFactory extends BaseFactory {


    private final String userAgent;
    private final TransferListener<? super DataSource> listener;
    private final int connectTimeoutMillis;
    private final int readTimeoutMillis;
    private final boolean allowCrossProtocolRedirects;

    /**
     * Constructs a DefaultHttpDataSourceFactory. Sets {@link
     * DefaultHttpDataSource#DEFAULT_CONNECT_TIMEOUT_MILLIS} as the connection timeout, {@link
     * DefaultHttpDataSource#DEFAULT_READ_TIMEOUT_MILLIS} as the read timeout and disables
     * cross-protocol redirects.
     *
     * @param userAgent The User-Agent string that should be used.
     */
    public DaolabHttpDataSourceFactory(String userAgent) {
        this(userAgent, null);
    }

    /**
     * Constructs a DefaultHttpDataSourceFactory. Sets {@link
     * DefaultHttpDataSource#DEFAULT_CONNECT_TIMEOUT_MILLIS} as the connection timeout, {@link
     * DefaultHttpDataSource#DEFAULT_READ_TIMEOUT_MILLIS} as the read timeout and disables
     * cross-protocol redirects.
     *
     * @param userAgent The User-Agent string that should be used.
     * @param listener An optional listener.
     * @see #DaolabHttpDataSourceFactory(String, TransferListener, int, int, boolean)
     */
    public DaolabHttpDataSourceFactory(
            String userAgent, TransferListener<? super DataSource> listener) {
        this(userAgent, listener, DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS, false);
    }

    /**
     * @param userAgent The User-Agent string that should be used.
     * @param listener An optional listener.
     * @param connectTimeoutMillis The connection timeout that should be used when requesting remote
     *     data, in milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param readTimeoutMillis The read timeout that should be used when requesting remote data, in
     *     milliseconds. A timeout of zero is interpreted as an infinite timeout.
     * @param allowCrossProtocolRedirects Whether cross-protocol redirects (i.e. redirects from HTTP
     *     to HTTPS and vice versa) are enabled.
     */
    public DaolabHttpDataSourceFactory(String userAgent,
                                        TransferListener<? super DataSource> listener, int connectTimeoutMillis,
                                        int readTimeoutMillis, boolean allowCrossProtocolRedirects) {
        this.userAgent = userAgent;
        this.listener = listener;
        this.connectTimeoutMillis = connectTimeoutMillis;
        this.readTimeoutMillis = readTimeoutMillis;
        this.allowCrossProtocolRedirects = allowCrossProtocolRedirects;
    }

    @Override
    protected DefaultHttpDataSource createDataSourceInternal(
            HttpDataSource.RequestProperties defaultRequestProperties) {
        defaultRequestProperties.set("Cookie", "DaolabPass=" + ConfigManager.getPassString());
        return new DefaultHttpDataSource(userAgent, null, listener, connectTimeoutMillis,
                readTimeoutMillis, allowCrossProtocolRedirects, defaultRequestProperties);
    }
}
