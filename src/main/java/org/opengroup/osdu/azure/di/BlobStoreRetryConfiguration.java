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

import com.azure.storage.common.policy.RequestRetryOptions;
import com.azure.storage.common.policy.RetryPolicyType;
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
 * Config for BlogStorage Retry.
 */
@Configuration
@ConfigurationProperties("azure.blobstore")
@Getter
@Setter
public class BlobStoreRetryConfiguration {

    private static final Logger LOGGER = LoggerFactory.getLogger(BlobStoreRetryConfiguration.class.getName());
    @Autowired
    private ILogger logger;

    private int maxTries = -1;
    private int tryTimeoutInSeconds = -1;
    private int retryDelayInMs = -1;
    private int maxRetryDelayInMs = -1;
    private String retryPolicyType = "";
    private String secondaryHost = "";

    /**
     * Checks whether an int variable value is configured or not.
     * @param val integer value to be checked
     * @return true if value is configured in app.properties
     */
    private boolean valueConfigured(final int val) {
        if (val != -1) {
            return true;
        }
        return false;
    }

    /**
     * Checks whether an string variable value is configured or not.
     * @param val string value to be checked
     * @return true if value is configured in app.properties
     */
    private boolean valueConfigured(final String val) {
        if (val == null || val.isEmpty()) {
            return false;
        }
        return true;
    }

    /**
     * Method to get RequestRetryOptions object based on configuration set in applicaiton.properties.
     * @return RequestRetryOption object with appropriate configurations.
     */
    public RequestRetryOptions getRequestRetryOptions() {

        // Check whether the variables have been set, else keep them as null.
        // Value has to be sent as null incase where they are not configured to use the default configurations (As specified in RequestRetryOptions.class)
        // https://azure.github.io/azure-storage-java-async/com/microsoft/azure/storage/blob/RequestRetryOptions.html

        RetryPolicyType rpt = valueConfigured(retryPolicyType) ? RetryPolicyType.valueOf(retryPolicyType) : RetryPolicyType.EXPONENTIAL;
        Integer maxTriesValue = valueConfigured(this.maxTries) ? this.maxTries : null;
        Duration tryTimeout = valueConfigured(tryTimeoutInSeconds) ? Duration.ofSeconds((long) tryTimeoutInSeconds) : null;
        Duration retryDelay = valueConfigured(retryDelayInMs) ? Duration.ofMillis(retryDelayInMs) : null;
        Duration maxRetryDelay = valueConfigured(maxRetryDelayInMs) ? Duration.ofMillis(maxRetryDelayInMs) : null;
        String secondaryHostValue = valueConfigured(this.secondaryHost) ? this.secondaryHost : null;

        RequestRetryOptions requestRetryOptions = new RequestRetryOptions(rpt, maxTriesValue, tryTimeout, retryDelay, maxRetryDelay, secondaryHostValue);


        this.logger.info("BlobStoreRetryConfiguration", String.format("Retry Options on BlobStorage with RetryPolicyType = %s , maxTries = %d , tryTimeout = %d , retryDelay = %d , maxRetryDelay = %d , secondaryHost = %s.",
                        rpt.toString(), requestRetryOptions.getMaxTries(), requestRetryOptions.getTryTimeoutDuration().getSeconds(), requestRetryOptions.getRetryDelay().toMillis(), requestRetryOptions.getMaxRetryDelay().toMillis(), requestRetryOptions.getSecondaryHost()), Collections.emptyMap());

        return requestRetryOptions;
    }
}
