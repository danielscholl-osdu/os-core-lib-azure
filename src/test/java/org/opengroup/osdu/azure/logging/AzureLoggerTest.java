package org.opengroup.osdu.azure.logging;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link AzureLogger}
 */
@ExtendWith(MockitoExtension.class)
public class AzureLoggerTest {
    private static final String LOGGER_NAME = AzureLoggerTest.class.getName();
    private static final String LOG_MESSAGE = "Hello world";
    private static final Exception EXCEPTION = new Exception();

    @Mock
    private JaxRsDpsLog jaxRsDpsLog;

    @InjectMocks
    private AzureLogger azureLogger;

    @Test
    public void testInfo() {
        doNothing().when(jaxRsDpsLog).info(eq(LOG_MESSAGE));
        azureLogger.info(LOGGER_NAME, LOG_MESSAGE);
        verify(jaxRsDpsLog, times(1)).info(eq(LOG_MESSAGE));
    }

    @Test
    public void testWarn() {
        doNothing().when(jaxRsDpsLog).warning(eq(LOG_MESSAGE));
        azureLogger.warn(LOGGER_NAME, LOG_MESSAGE);
        verify(jaxRsDpsLog, times(1)).warning(eq(LOG_MESSAGE));
    }

    @Test
    public void testWarnWithException() {
        doNothing().when(jaxRsDpsLog).warning(eq(LOG_MESSAGE), eq(EXCEPTION));
        azureLogger.warn(LOGGER_NAME, LOG_MESSAGE, EXCEPTION);
        verify(jaxRsDpsLog, times(1)).warning(eq(LOG_MESSAGE), eq(EXCEPTION));
    }

    @Test
    public void testError() {
        doNothing().when(jaxRsDpsLog).error(eq(LOG_MESSAGE));
        azureLogger.error(LOGGER_NAME, LOG_MESSAGE);
        verify(jaxRsDpsLog, times(1)).error(eq(LOG_MESSAGE));
    }

    @Test
    public void testErrorWithException() {
        doNothing().when(jaxRsDpsLog).error(eq(LOG_MESSAGE), eq(EXCEPTION));
        azureLogger.error(LOGGER_NAME, LOG_MESSAGE, EXCEPTION);
        verify(jaxRsDpsLog, times(1)).error(eq(LOG_MESSAGE), eq(EXCEPTION));
    }
}
