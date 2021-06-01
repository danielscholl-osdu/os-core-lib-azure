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

import com.microsoft.azure.documentdb.RetryOptions;
import lombok.Getter;
import lombok.Setter;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Class to set configuration for CosmosBulkExecutor.
 */
@Configuration
@ConfigurationProperties("azure.cosmosbulk")
@Getter
@Setter
public class CosmosBulkRetryConfiguration {
    public static final String LOGGER_NAME = CosmosBulkRetryConfiguration.class.getName();

    private RetryOptions defaultRetryOptions = new RetryOptions();

    private int maxRetryAttemptsOnThrottledRequests = defaultRetryOptions.getMaxRetryAttemptsOnThrottledRequests();
    private int maxRetryWaitTimeInSeconds = defaultRetryOptions.getMaxRetryWaitTimeInSeconds();
    private long retryWithInitialBackoffTimeInMilliseconds = defaultRetryOptions.getRetryWithInitialBackoffTime();
    private int retryWithBackoffMultiplier = defaultRetryOptions.getRetryWithBackoffMultiplier();

    /**
     * Create RetryOptions object based on application.properties configuration.
     * @return object of RetryOptions
     */
    public RetryOptions getRetryOptions() {
        RetryOptions retryOptions = new RetryOptions();

        retryOptions.setMaxRetryAttemptsOnThrottledRequests(maxRetryAttemptsOnThrottledRequests);
        retryOptions.setMaxRetryWaitTimeInSeconds(maxRetryWaitTimeInSeconds);
        retryOptions.setRetryWithInitialBackoffTime(retryWithInitialBackoffTimeInMilliseconds);
        retryOptions.setRetryWithBackoffMultiplier(retryWithBackoffMultiplier);

        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).info("Retry Options on CosmosBulkExecutorClient with maxRetryAttempts = {} , MaxRetryWaitTime = {} , retryWithInitialBackOffTime = {} , retryWithBackoffMultiplier = {}", retryOptions.getMaxRetryAttemptsOnThrottledRequests(), retryOptions.getMaxRetryWaitTimeInSeconds(), retryOptions.getRetryWithInitialBackoffTime(), retryOptions.getRetryWithBackoffMultiplier());

        return retryOptions;

    }
}
