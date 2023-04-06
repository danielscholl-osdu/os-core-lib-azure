package org.opengroup.osdu.azure.di;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * A configuration bean class to set up redis cache related variables.
 */
@ConfigurationProperties
@AllArgsConstructor
@Getter
public class RedisAzureConfiguration {
    private int database;
    private int expiration;
    private int port;
    private long timeout;
    private String hostKey;
    private String passwordKey;

    /**
     * Constructor with default hostKey and passwordKey.
     * @param pDatabase database
     * @param pExpiration expiration
     * @param pPort port
     * @param pTimeout timeout
     */
    public RedisAzureConfiguration(final int pDatabase, final int pExpiration, final int pPort, final long pTimeout) {
        this(pDatabase, pExpiration, pPort, pTimeout, "redis-hostname", "redis-password");
    }
}
