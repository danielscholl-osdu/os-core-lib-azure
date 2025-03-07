/** Copyright © Microsoft Corporation

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
**/

package org.opengroup.osdu.azure.di;

import com.azure.cosmos.ThrottlingRetryOptions;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

/**
 * CosmosRetryConfiguration settings.
 */
@Configuration
@ConfigurationProperties("azure.cosmos")
@Getter
@Setter
public class CosmosRetryConfiguration {
    public static final String LOGGER_NAME = CosmosRetryConfiguration.class.getName();

    private ThrottlingRetryOptions defaultThrottlingRetryOptions = new ThrottlingRetryOptions();

    /**
     * Value for max Retry Count on Throttled Requests for Cosmos.
     */
    private int maxRetryCount = defaultThrottlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(); // Sets the  default value for axRetryAttemptsOnThrottledRequests
    /**
     * Value for max retry wait time for Cosmos (Value in seconds).
     */
    private long retryWaitTimeout = defaultThrottlingRetryOptions.getMaxRetryWaitTime().getSeconds(); // Sets the  default value for MaxRetryWaitTime in seconds

    /**
     * Set's the Throttling retry options based on application.properties configuration.
     * @return Throttling retry options
     */
    public ThrottlingRetryOptions getThrottlingRetryOptions() {
        ThrottlingRetryOptions throttlingRetryOptions = new ThrottlingRetryOptions();

        throttlingRetryOptions.setMaxRetryAttemptsOnThrottledRequests(this.getMaxRetryCount());
        throttlingRetryOptions.setMaxRetryWaitTime(Duration.ofSeconds(this.getRetryWaitTimeout()));

        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).info("Retry Options on CosmosClient with maxRetryAttempts = {} , MaxRetryWaitTime = {}.", throttlingRetryOptions.getMaxRetryAttemptsOnThrottledRequests(), throttlingRetryOptions.getMaxRetryWaitTime());

        return throttlingRetryOptions;
    }
}
