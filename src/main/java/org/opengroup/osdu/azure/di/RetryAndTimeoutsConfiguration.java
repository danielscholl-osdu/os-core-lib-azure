package org.opengroup.osdu.azure.di;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration bean for setting up retry and timeouts variables.
 */

@Data
@ConfigurationProperties(prefix = "azure.retryAndTimeout")
public class RetryAndTimeoutsConfiguration {

    private int retryCountForServiceUnavailableStrategy = 3;
    private int connectTimeoutInMillis = 60000;
    private int connectionRequestTimeout = 60000;
    private int socketTimeout = 60000;
}
