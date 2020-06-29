package org.opengroup.osdu.azure.logging;

import org.opengroup.osdu.core.common.logging.JaxRsDpsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * A wrapper on {@link JaxRsDpsLog}.
 */
@Component
public class AzureLogger {
    @Autowired
    private JaxRsDpsLog jaxRsDpsLog;

    /**
     * To log info messages.
     * @param loggerName Name of the logger. Generally Fully qualified class name is used. e.g className.class
     * @param message Message to log
     */
    public void info(final String loggerName, final String message) {
        jaxRsDpsLog.info(message);
    }

    /**
     * To log info messages.
     * @param loggerName Name of the logger. Generally Fully qualified class name is used. e.g className.class
     * @param message Message to log
     */
    public void warn(final String loggerName, final String message) {
        jaxRsDpsLog.warning(message);
    }

    /**
     * To log info messages.
     * @param loggerName Name of the logger. Generally Fully qualified class name is used. e.g className.class
     * @param message Message to log
     * @param e exception to log
     */
    public void warn(final String loggerName, final String message, final Exception e) {
        jaxRsDpsLog.warning(message, e);
    }

    /**
     * To log info messages.
     * @param loggerName Name of the logger. Generally Fully qualified class name is used. e.g className.class
     * @param message Message to log
     */
    public void error(final String loggerName, final String message) {
        jaxRsDpsLog.error(message);
    }

    /**
     * To log info messages.
     * @param loggerName Name of the logger. Generally Fully qualified class name is used. e.g className.class
     * @param message Message to log
     * @param e exception to log
     */
    public void error(final String loggerName, final String message, final Exception e) {
        jaxRsDpsLog.error(message, e);
    }
}
