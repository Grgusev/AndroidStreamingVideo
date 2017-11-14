

package com.daolab.daolabplayer.player.metadata;

/**
 * Created by almond on 09/04/2017.
 */

public class PKBinaryFrame implements PKMetadata {

    public final String id;
    public final byte[] data;

    public PKBinaryFrame(String id, byte[] data) {
        this.id = id;
        this.data = data;
    }
}
