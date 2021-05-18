package org.opengroup.osdu.azure.di;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;

/**
 * CosmosRetryConfiguration settings.
 */
@Configuration
public class CosmosRetryConfiguration {

    @Value("${azure.blobStore.retrySupported}")
    private boolean retrySupported;

    @Value("${azure.cosmos.retryOptions.MaxRetryAttempts}")
    private int retryCount;

    @Value("${azure.cosmos.retryOptions.MaxRetryWaitTime}")
    private int retryWaitTimeout;

    /**
     *
     * @return retrySupported
     */
    public boolean isRetrySupported() {
        return retrySupported;
    }

    /**
     *
     * @return retryCount
     */
    public int getRetryCount() {
        return retryCount;
    }

    /**
     *
     * @return retryWaitTimeout
     */
    public int getRetryWaitTimeout() {
        return retryWaitTimeout;
    }
}
