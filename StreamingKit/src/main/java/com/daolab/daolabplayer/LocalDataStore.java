

package com.daolab.daolabplayer;

import java.io.FileNotFoundException;

/**
 * Storage interface that is passed to {@link LocalAssetsManager}
 * in order to save/load/remove the offline Drm keySetId.
 * Created by almond on almond
 */
public interface LocalDataStore {

    /**
     * Save the offline drm keySetId.
     * @param key - the key to save the value.
     * @param value - the keySetId of the drm.
     */
    void save(String key, byte[] value);

    /**
     * Loads the keySetId.
     * @param key - key for the keySetId.
     * @return - the keySetId to the drm.
     * @throws FileNotFoundException - thrown when the keySetId could not be found with specified key.
     */
    byte[] load(String key) throws FileNotFoundException;

    /**
     * Remove the keySetId from the storage.
     * @param key - key that should be removed.
     */
    void remove(String key);
}
