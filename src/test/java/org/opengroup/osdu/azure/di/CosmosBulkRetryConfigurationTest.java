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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.azure.logging.CoreLogger;
import org.opengroup.osdu.azure.logging.CoreLoggerFactory;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;
import static org.mockito.MockitoAnnotations.openMocks;
import static org.junit.jupiter.api.Assertions.assertEquals;


@ExtendWith(MockitoExtension.class)
public class CosmosBulkRetryConfigurationTest {
    @Mock
    private CoreLoggerFactory coreLoggerFactory;
    @Mock
    private CoreLogger coreLogger;
    @InjectMocks
    private CosmosBulkRetryConfiguration cosmosBulkRetryConfiguration;

    /**
     * Workaround for inability to mock static methods like getInstance().
     *
     * @param mock CoreLoggerFactory mock instance
     */
    private void mockSingleton(CoreLoggerFactory mock) {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, mock);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Reset workaround for inability to mock static methods like getInstance().
     */
    private void resetSingleton() {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(null, null);
            instance.setAccessible(false);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    void setUp() {
        openMocks(this);
        mockSingleton(coreLoggerFactory);
        when(coreLoggerFactory.getLogger(anyString())).thenReturn(coreLogger);
    }

    @AfterEach
    public void takeDown() {
        resetSingleton();
    }

    private RetryOptions defaultRetryOptions = new RetryOptions();

    @Test
    public void should_set_max_retry_attempts() {
        cosmosBulkRetryConfiguration.setMaxRetryAttemptsOnThrottledRequests(30);
        RetryOptions retryOptions = cosmosBulkRetryConfiguration.getRetryOptions();
        assertEquals(retryOptions.getMaxRetryAttemptsOnThrottledRequests(),30);
        assertEquals(cosmosBulkRetryConfiguration.getMaxRetryWaitTimeInSeconds(),defaultRetryOptions.getMaxRetryWaitTimeInSeconds());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithInitialBackoffTimeInMilliseconds(),defaultRetryOptions.getRetryWithInitialBackoffTime());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithBackoffMultiplier(),defaultRetryOptions.getRetryWithBackoffMultiplier());
    }
    @Test
    public void should_set_max_retry_wait_timeout() {
        cosmosBulkRetryConfiguration.setMaxRetryWaitTimeInSeconds(45);
        RetryOptions retryOptions = cosmosBulkRetryConfiguration.getRetryOptions();
        assertEquals(retryOptions.getMaxRetryAttemptsOnThrottledRequests(),defaultRetryOptions.getMaxRetryAttemptsOnThrottledRequests());
        assertEquals(cosmosBulkRetryConfiguration.getMaxRetryWaitTimeInSeconds(),45);
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithInitialBackoffTimeInMilliseconds(),defaultRetryOptions.getRetryWithInitialBackoffTime());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithBackoffMultiplier(),defaultRetryOptions.getRetryWithBackoffMultiplier());
    }

    @Test
    public void should_set_retry_with_initial_backoff() {
        cosmosBulkRetryConfiguration.setRetryWithInitialBackoffTimeInMilliseconds(50);
        RetryOptions retryOptions = cosmosBulkRetryConfiguration.getRetryOptions();
        assertEquals(retryOptions.getMaxRetryAttemptsOnThrottledRequests(),defaultRetryOptions.getMaxRetryAttemptsOnThrottledRequests());
        assertEquals(cosmosBulkRetryConfiguration.getMaxRetryWaitTimeInSeconds(),defaultRetryOptions.getMaxRetryWaitTimeInSeconds());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithInitialBackoffTimeInMilliseconds(),50);
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithBackoffMultiplier(),defaultRetryOptions.getRetryWithBackoffMultiplier());
    }

    @Test
    public void should_set_retry_with_backoff_multiplier() {
        cosmosBulkRetryConfiguration.setRetryWithBackoffMultiplier(3);
        RetryOptions retryOptions = cosmosBulkRetryConfiguration.getRetryOptions();
        assertEquals(retryOptions.getMaxRetryAttemptsOnThrottledRequests(),defaultRetryOptions.getMaxRetryAttemptsOnThrottledRequests());
        assertEquals(cosmosBulkRetryConfiguration.getMaxRetryWaitTimeInSeconds(),defaultRetryOptions.getMaxRetryWaitTimeInSeconds());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithInitialBackoffTimeInMilliseconds(),defaultRetryOptions.getRetryWithInitialBackoffTime());
        assertEquals(cosmosBulkRetryConfiguration.getRetryWithBackoffMultiplier(),3);
    }

}
