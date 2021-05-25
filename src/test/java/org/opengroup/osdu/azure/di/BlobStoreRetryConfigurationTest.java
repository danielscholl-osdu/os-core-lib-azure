package org.opengroup.osdu.azure.di;
import com.azure.cosmos.ThrottlingRetryOptions;
import com.azure.storage.common.policy.RequestRetryOptions;
import io.jsonwebtoken.lang.Assert;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.ILogger;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.Duration;
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

    boolean compareStrings(String str1, String str2) {
        if (str1 == null || str2 == null)
            return str1 == str2;

        return str1.equals(str2);
    }

    @Test
    public void should_set_default_values() {
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        Assert.isTrue(requestRetryOptions.getMaxTries() == defaultRequestRetryOptions.getMaxTries());
        Assert.isTrue(requestRetryOptions.getTryTimeoutDuration().equals(defaultRequestRetryOptions.getTryTimeoutDuration()));
        Assert.isTrue(requestRetryOptions.getRetryDelay().equals(defaultRequestRetryOptions.getRetryDelay()));
        Assert.isTrue(requestRetryOptions.getMaxRetryDelay().equals(defaultRequestRetryOptions.getMaxRetryDelay()));
        Assert.isTrue(compareStrings(requestRetryOptions.getSecondaryHost(),defaultRequestRetryOptions.getSecondaryHost()));
    }

    @Test
    public void should_set_maxtries() {
        int maxTriesValue = 10;
        blobStoreRetryConfiguration.setMaxTries(maxTriesValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        Assert.isTrue(requestRetryOptions.getMaxTries() == maxTriesValue);
        Assert.isTrue(requestRetryOptions.getTryTimeoutDuration().equals(defaultRequestRetryOptions.getTryTimeoutDuration()));
        Assert.isTrue(requestRetryOptions.getRetryDelay().equals(defaultRequestRetryOptions.getRetryDelay()));
        Assert.isTrue(requestRetryOptions.getMaxRetryDelay().equals(defaultRequestRetryOptions.getMaxRetryDelay()));
        Assert.isTrue(compareStrings(requestRetryOptions.getSecondaryHost(),defaultRequestRetryOptions.getSecondaryHost()));
    }

    @Test
    public void should_set_try_timeout() {
        int tryTimeoutValue = 50;
        blobStoreRetryConfiguration.setTryTimeoutInSeconds(tryTimeoutValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        Assert.isTrue(requestRetryOptions.getMaxTries() == defaultRequestRetryOptions.getMaxTries());
        Assert.isTrue(requestRetryOptions.getTryTimeoutDuration().equals(Duration.ofSeconds(tryTimeoutValue)));
        Assert.isTrue(requestRetryOptions.getRetryDelay().equals(defaultRequestRetryOptions.getRetryDelay()));
        Assert.isTrue(requestRetryOptions.getMaxRetryDelay().equals(defaultRequestRetryOptions.getMaxRetryDelay()));
        Assert.isTrue(compareStrings(requestRetryOptions.getSecondaryHost(),defaultRequestRetryOptions.getSecondaryHost()));
    }

    @Test
    public void should_set_RetryDelay() {
        int retryDelayValue = 50;
        int maxRetryDelayValue =100;
        blobStoreRetryConfiguration.setRetryDelayInMs(retryDelayValue);
        blobStoreRetryConfiguration.setMaxRetryDelayInMs(maxRetryDelayValue);
        RequestRetryOptions requestRetryOptions = blobStoreRetryConfiguration.getRequestRetryOptions();

        Assert.isTrue(requestRetryOptions.getMaxTries() == defaultRequestRetryOptions.getMaxTries());
        Assert.isTrue(requestRetryOptions.getTryTimeoutDuration().equals(defaultRequestRetryOptions.getTryTimeoutDuration()));
        Assert.isTrue(requestRetryOptions.getRetryDelay().equals(Duration.ofMillis(retryDelayValue)));
        Assert.isTrue(requestRetryOptions.getMaxRetryDelay().equals(Duration.ofMillis(maxRetryDelayValue)));
        Assert.isTrue(compareStrings(requestRetryOptions.getSecondaryHost(),defaultRequestRetryOptions.getSecondaryHost()));
    }

}
