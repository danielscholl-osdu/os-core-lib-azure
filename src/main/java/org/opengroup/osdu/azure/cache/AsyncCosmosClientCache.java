package org.opengroup.osdu.azure.cache;

import com.azure.cosmos.internal.AsyncDocumentClient;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation of ICache for AsyncDocumentClient.
 */
@Component
@Lazy
public class AsyncCosmosClientCache extends VmCache<String, AsyncDocumentClient> {

    /**
     *  Default cache constructor.
     */
    public AsyncCosmosClientCache() {
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
