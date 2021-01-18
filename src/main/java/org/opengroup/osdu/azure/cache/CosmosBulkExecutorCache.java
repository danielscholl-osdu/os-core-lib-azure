package org.opengroup.osdu.azure.cache;

import com.microsoft.azure.documentdb.bulkexecutor.DocumentBulkExecutor;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation of ICache for DocumentBulkExecutor.
 */
@Component
@Lazy
public class CosmosBulkExecutorCache extends VmCache<String, DocumentBulkExecutor> {

    /**
     *  Default cache constructor.
     */
    public CosmosBulkExecutorCache() {
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