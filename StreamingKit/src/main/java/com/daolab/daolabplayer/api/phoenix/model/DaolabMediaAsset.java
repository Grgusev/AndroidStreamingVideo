

package com.daolab.daolabplayer.api.phoenix.model;

import com.daolab.netkit.connect.response.BaseResult;

import java.util.List;

/**
 */

public class DaolabMediaAsset extends BaseResult {
    int id;
    int type;
    String name;
    List<DaolabMediaFile> mediaFiles;

    public int getId() {
        return id;
    }

    public int getType() {
        return type;
    }

    public List<DaolabMediaFile> getFiles() {
        return mediaFiles;
    }

}
