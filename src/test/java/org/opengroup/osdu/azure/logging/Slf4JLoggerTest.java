package org.opengroup.osdu.azure.logging;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.model.http.HeadersToLog;
import org.opengroup.osdu.core.common.model.http.Request;
import org.slf4j.Logger;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Contains tests for {@link Slf4JLogger}
 */
@ExtendWith(MockitoExtension.class)
public class Slf4JLoggerTest {
    private static final String LOG_PREFIX = "services.app";
    private static final String DEFAULT_LOGGER_NAME = Slf4JLogger.class.getName();
    private static final String LOGGER_NAME = Slf4JLoggerTest.class.getName();
    private static final String LOG_MESSAGE = "Hello world !";

    @Mock
    private HeadersToLog headersToLog;

    @Mock
    private AuditPayload auditPayload;

    @Mock
    private Slf4jLoggerFactory slf4jLoggerFactory;

    @Mock
    private Request request;

    @Mock
    private Logger logger;

    @Mock
    private Exception e;

    private final Map<String, String> headers = new HashMap<>();

    @InjectMocks
    Slf4JLogger slf4JLogger;

    @BeforeEach
    public void setup() {
        when(slf4jLoggerFactory.getLogger(any())).thenReturn(logger);
        when(headersToLog.createStandardLabelsFromMap(eq(headers))).thenReturn(headers);
    }

    @Test
    public void testAudit() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(auditPayload), eq(headers));
        slf4JLogger.audit(LOG_PREFIX, auditPayload, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(auditPayload), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testAuditWithLoggerName() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(auditPayload), eq(headers));
        slf4JLogger.audit(LOGGER_NAME, LOG_PREFIX, auditPayload, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(auditPayload), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testRequest() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(request), eq(headers));
        slf4JLogger.request(LOG_PREFIX, request, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(request), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testRequestWithLoggerName() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(request), eq(headers));
        slf4JLogger.request(LOGGER_NAME, LOG_PREFIX, request, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(request), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testInfo() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.info(LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testInfoWithLoggerName() {
        doNothing().when(logger).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.info(LOGGER_NAME, LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).info(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testWarning() {
        doNothing().when(logger).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.warning(LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testWarningWithLoggerName() {
        doNothing().when(logger).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.warning(LOGGER_NAME, LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testWarningWithException() {
        doNothing().when(logger).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        slf4JLogger.warning(LOG_PREFIX, LOG_MESSAGE, e, headers);
        verify(logger, times(1)).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testWarningWithExceptionAndLoggerName() {
        doNothing().when(logger).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        slf4JLogger.warning(LOGGER_NAME, LOG_PREFIX, LOG_MESSAGE, e, headers);
        verify(logger, times(1)).warn(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testError() {
        doNothing().when(logger).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.error(LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testErrorWithLoggerName() {
        doNothing().when(logger).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        slf4JLogger.error(LOGGER_NAME, LOG_PREFIX, LOG_MESSAGE, headers);
        verify(logger, times(1)).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testErrorWithException() {
        doNothing().when(logger).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        slf4JLogger.error(LOG_PREFIX, LOG_MESSAGE, e, headers);
        verify(logger, times(1)).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(DEFAULT_LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }

    @Test
    public void testErrorWithExceptionAndLoggerName() {
        doNothing().when(logger).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        slf4JLogger.error(LOGGER_NAME, LOG_PREFIX, LOG_MESSAGE, e, headers);
        verify(logger, times(1)).error(eq("{} {} {}"), eq(LOG_PREFIX), eq(LOG_MESSAGE), eq(headers), eq(e));
        verify(slf4jLoggerFactory, times(1)).getLogger(eq(LOGGER_NAME));
        verify(headersToLog, times(1)).createStandardLabelsFromMap(eq(headers));
    }
}
