package org.opengroup.osdu.azure.logging;

import org.opengroup.osdu.core.common.logging.ILogger;
import org.opengroup.osdu.core.common.logging.audit.AuditPayload;
import org.opengroup.osdu.core.common.model.http.HeadersToLog;
import org.opengroup.osdu.core.common.model.http.Request;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import java.util.Map;

/**
 * {@link ILogger} implementation with Slf4J Logger.
 */
@Component
@Primary
@ConditionalOnProperty(value = "logging.slf4jlogger.enabled", havingValue = "true", matchIfMissing = false)
public final class Slf4JLogger implements ILogger {
    private static final String DEFAULT_LOGGER_NAME = Slf4JLogger.class.getName();

    @Autowired
    private HeadersToLog headersToLog;

    @Autowired
    private Slf4jLoggerFactory slf4jLoggerFactory;

    @Override
    public void audit(final String logPrefix, final AuditPayload auditPayload, final Map<String, String> headers) {
        this.audit(DEFAULT_LOGGER_NAME, logPrefix, auditPayload, headers);
    }

    @Override
    public void audit(final String loggerName, final String logPrefix, final AuditPayload payload, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).info("{} {} {}", logPrefix, payload,
                this.headersToLog.createStandardLabelsFromMap(headers));
    }

    @Override
    public void request(final String logPrefix, final Request request, final Map<String, String> headers) {
        this.request(DEFAULT_LOGGER_NAME, logPrefix, request, headers);
    }

    @Override
    public void request(final String loggerName, final String logPrefix, final Request request, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).info("{} {} {}", logPrefix, request,
                this.headersToLog.createStandardLabelsFromMap(headers));
    }

    @Override
    public void info(final String logPrefix, final String message, final Map<String, String> headers) {
        this.info(DEFAULT_LOGGER_NAME, logPrefix, message, headers);
    }

    @Override
    public void info(final String loggerName, final String logPrefix, final String message, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).info("{} {} {}", logPrefix, message,
                this.headersToLog.createStandardLabelsFromMap(headers));
    }

    @Override
    public void warning(final String logPrefix, final String message, final Map<String, String> headers) {
        this.warning(DEFAULT_LOGGER_NAME, logPrefix, message, headers);
    }

    @Override
    public void warning(final String loggerName, final String logPrefix, final String message, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).warn("{} {} {}", logPrefix, message,
                this.headersToLog.createStandardLabelsFromMap(headers));
    }

    @Override
    public void warning(final String logPrefix, final String message, final Exception e, final Map<String, String> headers) {
        this.warning(DEFAULT_LOGGER_NAME, logPrefix, message, e, headers);
    }

    @Override
    public void warning(final String loggerName, final String logPrefix, final String message, final Exception ex, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).warn("{} {} {}", logPrefix, message,
                this.headersToLog.createStandardLabelsFromMap(headers), ex);
    }

    @Override
    public void error(final String logPrefix, final String message, final Map<String, String> headers) {
        this.error(DEFAULT_LOGGER_NAME, logPrefix, message, headers);
    }

    @Override
    public void error(final String loggerName, final String logPrefix, final String message, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).error("{} {} {}", logPrefix, message,
                this.headersToLog.createStandardLabelsFromMap(headers));
    }

    @Override
    public void error(final String logPrefix, final String message, final Exception e,
                      final Map<String, String> headers) {
        this.error(DEFAULT_LOGGER_NAME, logPrefix, message, e, headers);
    }

    @Override
    public void error(final String loggerName, final String logPrefix, final String message, final Exception ex, final Map<String, String> headers) {
        slf4jLoggerFactory.getLogger(loggerName).error("{} {} {}", logPrefix, message,
                this.headersToLog.createStandardLabelsFromMap(headers), ex);
    }

    @Override
    public void close() throws Exception {
        // do nothing
    }
}
