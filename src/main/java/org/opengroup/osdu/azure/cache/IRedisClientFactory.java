package org.opengroup.osdu.azure.cache;

import org.opengroup.osdu.azure.di.RedisAzureConfiguration;
import org.opengroup.osdu.core.common.cache.IRedisCache;

/**
 * Redis client creator.
 * @param <K>
 * @param <V>
 */
public interface IRedisClientFactory<K, V> {

    /**
     * Retrieve redis client. Creates a new client if not exists.
     * @param keyClass key class type
     * @param valueClass value class type
     * @param redisConfiguration configuration for redis client
     * @return redis cache client instance.
     */
    IRedisCache<K, V> getClient(Class<K> keyClass, Class<V> valueClass, RedisAzureConfiguration redisConfiguration);
}
