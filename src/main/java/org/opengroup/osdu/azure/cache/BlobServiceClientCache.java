package org.opengroup.osdu.azure.cache;

import com.azure.storage.blob.BlobServiceClient;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.core.common.cache.enums.CachingStrategy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation of ICache for BlobServiceClient.
 */
@Component
@Lazy
public class BlobServiceClientCache extends VmCache<String, BlobServiceClient> {

    /**
     *  Default cache constructor.
     */
    public BlobServiceClientCache() {
        super(60 * 60, 1000, CachingStrategy.EXPIRE_AFTER_WRITE);
    }

    /**
     * @param key cache key
     * @return true if found in cache
     */
    public boolean containsKey(final String key) {
        return this.get(key) != null;
    }
}
