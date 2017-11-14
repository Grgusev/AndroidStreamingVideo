

package com.daolab.daolabplayer.player.metadata;

import java.util.List;

/**
 * Created by almond on 09/04/2017.
 */

public class PKChapterTocFrame implements PKMetadata {

    public final String id;
    public final String elementId;
    public final boolean isRoot;
    public final boolean isOrdered;

    public final List<String> children;
    public final List<PKMetadata> subFrames;

    public PKChapterTocFrame(String id, String elementId, boolean isRoot, boolean isOrdered, List<String> children, List<PKMetadata> subFrames) {
        this.id = id;
        this.elementId = elementId;
        this.isRoot = isRoot;
        this.isOrdered = isOrdered;
        this.children = children;
        this.subFrames = subFrames;
    }

}
