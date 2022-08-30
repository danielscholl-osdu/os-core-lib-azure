package org.opengroup.osdu.azure.logging;

import org.springframework.stereotype.Component;

import java.time.Duration;

/**
 * Dependency logger.
 */
@Component
public class DependencyLogger {

    private static final String LOGGER_NAME = DependencyLogger.class.getName();

    /**
     * Log dependency.
     *
     * @param options the dependency logging options
     */
    public void logDependency(final DependencyLoggingOptions options) {
        DependencyPayload payload = new DependencyPayload();
        payload.setName(options.getName());
        payload.setData(options.getData());
        payload.setDuration(Duration.ofMillis(options.getTimeTakenInMs()));
        payload.setRequestCharge(options.getRequestCharge());
        payload.setResultCode(String.valueOf(options.getResultCode()));
        payload.setSuccess(options.isSuccess());
        payload.setType(options.getType());
        payload.setTarget(options.getTarget());

        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).logDependency(payload);
    }

    /**
     * Return a string composed of database name and collection.
     *
     * @param databaseName the Cosmos database name
     * @param collection   the Cosmos collection name
     * @return the dependency target string
     */
    public static String getCosmosDependencyTarget(final String databaseName, final String collection) {
        return String.format("%s/%s", databaseName, collection);
    }
}
