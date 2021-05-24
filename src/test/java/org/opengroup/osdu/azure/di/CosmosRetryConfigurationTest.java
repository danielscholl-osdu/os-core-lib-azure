package org.opengroup.osdu.azure.di;

import com.azure.cosmos.ThrottlingRetryOptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

@ExtendWith(MockitoExtension.class)
public class CosmosRetryConfigurationTest {

    @Spy
    CosmosRetryConfiguration cosmosRetryConfiguration;

    @BeforeEach
    void setUp() {
        initMocks(this);
    }

    @Test
    public void should_set_maxRetryCount_ThrottlingOptions() {
        cosmosRetryConfiguration.setMaxRetryCount(1);
        ThrottlingRetryOptions throttlingRetryOptions = cosmosRetryConfiguration.setThrottlingRetryOptions();
        verify(cosmosRetryConfiguration,times(1)).getMaxRetryCount();
        verify(cosmosRetryConfiguration,never()).getRetryWaitTimeout();
    }

    @Test
    public void should_set_RetryWaitTimeout_ThrottlingOptions() {
        cosmosRetryConfiguration.setRetryWaitTimeout(20);
        cosmosRetryConfiguration.setThrottlingRetryOptions();
        verify(cosmosRetryConfiguration,never()).getMaxRetryCount();
        verify(cosmosRetryConfiguration,times(1)).getRetryWaitTimeout();
    }

    @Test
    public void should_set_RetryWaitTimeout_MaxRetry_ThrottlingOptions() {
        cosmosRetryConfiguration.setRetryWaitTimeout(20);
        cosmosRetryConfiguration.setMaxRetryCount(2);
        cosmosRetryConfiguration.setThrottlingRetryOptions();
        verify(cosmosRetryConfiguration,times(1)).getMaxRetryCount();
        verify(cosmosRetryConfiguration,times(1)).getRetryWaitTimeout();
    }

    @Test
    public void should_not_set_RetryWaitTimeout_MaxRetry_ThrottlingOptions() {
        if(cosmosRetryConfiguration == null) {
            System.out.println("it is null");
        }
        cosmosRetryConfiguration.setThrottlingRetryOptions();
        verify(cosmosRetryConfiguration,never()).getMaxRetryCount();
        verify(cosmosRetryConfiguration,never()).getRetryWaitTimeout();
    }
}
