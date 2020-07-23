package org.opengroup.osdu.azure.logging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Factory for slf4j logger instances.
 */
@Component
public final class Slf4jLoggerFactory {
    /**
     * Returns slf4j logger instance based on name.
     * @param loggerName name of the logger
     * @return {@link Logger} instance
     */
    public Logger getLogger(final String loggerName) {
        return LoggerFactory.getLogger(loggerName);
    }
}
