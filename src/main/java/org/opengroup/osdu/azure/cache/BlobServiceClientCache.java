package org.opengroup.osdu.azure.cache;

import com.azure.storage.blob.BlobServiceClient;
import org.opengroup.osdu.core.common.cache.VmCache;

/**
 * Implementation of ICache for BlobServiceClient.
 */
public class BlobServiceClientCache extends VmCache<String, BlobServiceClient> {

    /**
     *  Default cache constructor.
     */
    public BlobServiceClientCache() {
        super(60 * 60, 1000);
    }

    /**
     * @param key cache key
     * @return true if found in cache
     */
    public boolean containsKey(final String key) {
        return this.get(key) != null;
    }
}
