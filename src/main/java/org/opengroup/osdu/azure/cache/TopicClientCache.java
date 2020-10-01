package org.opengroup.osdu.azure.cache;

import com.microsoft.azure.servicebus.TopicClient;
import org.opengroup.osdu.core.common.cache.VmCache;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

/**
 * Implementation of ICache for TopicClient.
 */
@Component
@Lazy
public class TopicClientCache extends VmCache<String, TopicClient> {

    /**
     *  Default cache constructor.
     */
    public TopicClientCache() {
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