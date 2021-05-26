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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.ILogger;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.MockitoAnnotations.openMocks;

@ExtendWith(MockitoExtension.class)
public class BlobStoreRetryConfigurationTest {
    @InjectMocks
    private BlobStoreRetryConfiguration blobStoreRetryConfiguration;
    @Mock
    private ILogger logger;
    RequestRetryOptions defaultRequestRetryOptions = new RequestRetryOptions();

    @BeforeEach
    void setUp() {
        openMocks(this);
    }

    @Test
    public void should_set_default_values() {
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        assertEquals(requestRetryOptions.getMaxTries(), defaultRequestRetryOptions.getMaxTries());
        assertEquals(requestRetryOptions.getTryTimeoutDuration(), defaultRequestRetryOptions.getTryTimeoutDuration());
        assertEquals(requestRetryOptions.getRetryDelay(), defaultRequestRetryOptions.getRetryDelay());
        assertEquals(requestRetryOptions.getMaxRetryDelay(), defaultRequestRetryOptions.getMaxRetryDelay());
        assertEquals(requestRetryOptions.getSecondaryHost(), defaultRequestRetryOptions.getSecondaryHost());
    }

    @Test
    public void should_set_maxtries() {
        int maxTriesValue = 10;
        blobStoreRetryConfiguration.setMaxTries(maxTriesValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        assertEquals(requestRetryOptions.getMaxTries(), maxTriesValue);
        assertEquals(requestRetryOptions.getTryTimeoutDuration(), defaultRequestRetryOptions.getTryTimeoutDuration());
        assertEquals(requestRetryOptions.getRetryDelay(), defaultRequestRetryOptions.getRetryDelay());
        assertEquals(requestRetryOptions.getMaxRetryDelay(), defaultRequestRetryOptions.getMaxRetryDelay());
        assertEquals(requestRetryOptions.getSecondaryHost(), defaultRequestRetryOptions.getSecondaryHost());
    }

    @Test
    public void should_set_try_timeout() {
        int tryTimeoutValue = 50;
        blobStoreRetryConfiguration.setTryTimeoutInSeconds(tryTimeoutValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        assertEquals(requestRetryOptions.getMaxTries(), defaultRequestRetryOptions.getMaxTries());
        assertEquals(requestRetryOptions.getTryTimeoutDuration(), Duration.ofSeconds(tryTimeoutValue));
        assertEquals(requestRetryOptions.getRetryDelay(), defaultRequestRetryOptions.getRetryDelay());
        assertEquals(requestRetryOptions.getMaxRetryDelay(), defaultRequestRetryOptions.getMaxRetryDelay());
        assertEquals(requestRetryOptions.getSecondaryHost(), defaultRequestRetryOptions.getSecondaryHost());
    }

    @Test
    public void should_set_RetryDelay() {
        int retryDelayValue = 50;
        int maxRetryDelayValue = 100;
        blobStoreRetryConfiguration.setRetryDelayInMs(retryDelayValue);
        blobStoreRetryConfiguration.setMaxRetryDelayInMs(maxRetryDelayValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        assertEquals(requestRetryOptions.getMaxTries(), defaultRequestRetryOptions.getMaxTries());
        assertEquals(requestRetryOptions.getTryTimeoutDuration(), defaultRequestRetryOptions.getTryTimeoutDuration());
        assertEquals(requestRetryOptions.getRetryDelay(), Duration.ofMillis(retryDelayValue));
        assertEquals(requestRetryOptions.getMaxRetryDelay(), Duration.ofMillis(maxRetryDelayValue));
        assertEquals(requestRetryOptions.getSecondaryHost(), defaultRequestRetryOptions.getSecondaryHost());
    }

}
