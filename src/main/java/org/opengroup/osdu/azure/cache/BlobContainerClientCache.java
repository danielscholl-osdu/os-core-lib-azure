package org.opengroup.osdu.azure.cache;

import com.azure.storage.blob.BlobContainerClient;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.opengroup.osdu.core.common.cache.enums.CachingStrategy;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation of ICache for BlobContainerClient.
 */
@Component
@Lazy
public class BlobContainerClientCache extends VmCache<String, BlobContainerClient> {

    /**
     *  Default cache constructor.
     */
    public BlobContainerClientCache() {
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