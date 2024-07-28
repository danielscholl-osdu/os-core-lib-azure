package org.opengroup.osdu.azure.logging;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.lang.reflect.Field;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Contains tests for {@link DependencyLogger}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
public class DependencyLoggerTest {

    private static final String DEFAULT_LOGGER_NAME = DependencyLogger.class.getName();

    @Mock
    private CoreLogger coreLogger;

    @Mock
    private CoreLoggerFactory coreLoggerFactory;

    @Mock
    private LogSamplerConfiguration logSamplerConfiguration;

    @Mock
    private DependencyLoggingOptions dependencyLoggingOptions;

    @Mock
    private DependencyPayload dependencyPayload;

    @InjectMocks
    DependencyLogger dependencyLogger;

    /**
     * Workaround for inability to mock static methods like getInstance().
     *
     * @param mock CoreLoggerFactory mock instance
     */
    private void mockSingleton(CoreLoggerFactory mock) {
        try {
            Field instance = CoreLoggerFactory.class.getDeclaredField("instance");
            instance.setAccessible(true);
            instance.set(instance, mock);
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
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @BeforeEach
    public void setup() {
        mockSingleton(coreLoggerFactory);
        when(coreLoggerFactory.getLogger(anyString())).thenReturn(coreLogger);
    }

    @AfterEach
    public void takeDown() {
        resetSingleton();
    }

    @Test
    public void testLogDependencyWithOptions() {
        doNothing().when(coreLogger).logDependency(any(DependencyPayload.class));
        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(100);
        dependencyLogger.logDependency(dependencyLoggingOptions);
        verify(coreLogger, times(1)).logDependency(any(DependencyPayload.class));
        verify(coreLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
    }

    @Test
    public void testLogDependencyWithPayload() {
        doNothing().when(coreLogger).logDependency(any(DependencyPayload.class));
        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(100);
        dependencyLogger.logDependencyWithPayload(dependencyPayload);
        verify(coreLogger, times(1)).logDependency(dependencyPayload);
        verify(coreLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
    }

    @Test
    public void testLogFailedDependencyWithOptionsWhenSampling() {
        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(0);
        when(dependencyPayload.isSuccess()).thenReturn(false);
        dependencyLogger.logDependency(dependencyLoggingOptions);
        verify(coreLogger, times(1)).logDependency(any(DependencyPayload.class));
        verify(coreLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
    }

    @Test
    public void testLogFailedDependencyWithPayloadWhenSampling() {
        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(0);
        when(dependencyPayload.isSuccess()).thenReturn(false);
        dependencyLogger.logDependencyWithPayload(dependencyPayload);
        verify(coreLogger, times(1)).logDependency(dependencyPayload);
        verify(coreLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
    }

//    @Test
//    public void testLogSuccessfulDependencyWithOptionsWhenSampling() {
//        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(0);
//        when(dependencyLoggingOptions.isSuccess()).thenReturn(true);
//        dependencyLogger.logDependency(dependencyLoggingOptions);
//        verify(coreLogger, times(0)).logDependency(any(DependencyPayload.class));
//        verify(coreLoggerFactory, times(0)).getLogger(eq(DEFAULT_LOGGER_NAME));
//    }
//
//    @Test
//    public void testLogSuccessfulDependencyWithPayloadWhenSampling() {
//        when(logSamplerConfiguration.getDependencySamplingPercentage()).thenReturn(0);
//        when(dependencyPayload.isSuccess()).thenReturn(true);
//        dependencyLogger.logDependencyWithPayload(dependencyPayload);
//        verify(coreLogger, times(0)).logDependency(dependencyPayload);
//        verify(coreLoggerFactory, times(0)).getLogger(eq(DEFAULT_LOGGER_NAME));
//    }
}
