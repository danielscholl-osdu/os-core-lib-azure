// Copyright © Microsoft Corporation
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//      http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

package org.opengroup.osdu.azure.di;

import com.azure.cosmos.ThrottlingRetryOptions;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.Collections;

/**
 * CosmosRetryConfiguration settings.
 */
@Configuration
@ConfigurationProperties("azure.cosmos")
@Getter
@Setter
public class CosmosRetryConfiguration {
    private static final Logger LOGGER = LoggerFactory.getLogger(CosmosRetryConfiguration.class.getName());
    @Autowired
    private ILogger logger;

    /**
     * Value for max Retry Count on Throttled Requests for Cosmos.
     */
    private int maxRetryCount = -1; // Setting default value of -1, indicates to use default maxRetryCount
    /**
     * Value for max retry wait time for Cosmos (Value in seconds).
     */
    private long retryWaitTimeout = -1; // Setting default value of -1, indicates to use default retryWaitTimeout

    /**
     *
     * @return Returns true if MaxRetryCount is configured.
     */
    public boolean isMaxRetryCountConfigured() {
        return maxRetryCount != -1;
    }

    /**
     *
     * @return Returns true if RetryWaitTimeout is configured.
     */
    public boolean isRetryWaitTimeoutConfigured() {
        return retryWaitTimeout != -1;
    }

    /**
     * Set's the Throttling retry options based on application.properties configuration.
     * @return Throttling retry options
     */
    public ThrottlingRetryOptions getThrottlingRetryOptions() {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();

        if (isMaxRetryCountConfigured()) {
            throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(this.getMaxRetryCount());
        }
        if (isRetryWaitTimeoutConfigured()) {
            throttlingRetryOptions.setMaxRetryWaitTime(Duration.ofSeconds(this.getRetryWaitTimeout()));
        }
        this.logger.info("CosmosRetryConfiguration", String.format("Retry Options on CosmosClient with maxRetryAttempts = {} , MaxRetryWaitTime = {}.", throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(), throttlingRetryOptions.getMaxRetryWaitTime()), Collections.emptyMap());

        return throttlingRetryOptions;
    }
}
