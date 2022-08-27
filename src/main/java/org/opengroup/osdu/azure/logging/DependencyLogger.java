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
     * @param name          the name of the command initiated with this dependency call
     * @param data          the command initiated by this dependency call
     * @param target        the target of this dependency call
     * @param timeTakenInMs the request duration in milliseconds
     * @param resultCode    the result code of the call
     * @param success       indication of successful or unsuccessful call
     */
    public void logDependency(final String name, final String data, final String target, final long timeTakenInMs, final int resultCode, final boolean success) {
        DependencyPayload payload = new DependencyPayload(name, data, Duration.ofMillis(timeTakenInMs), String.valueOf(resultCode), success);
        payload.setType("CosmosStore");
        payload.setTarget(target);

        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).logDependency(payload);
    }

    /**
     * Log dependency.
     *
     * @param name          the name of the command initiated with this dependency call
     * @param data          the command initiated by this dependency call
     * @param target        the target of this dependency call
     * @param timeTakenInMs the request duration in milliseconds
     * @param requestCharge the request charge
     * @param resultCode    the result code of the call
     * @param success       indication of successful or unsuccessful call
     */
    public void logDependency(final String name, final String data, final String target, final long timeTakenInMs, final double requestCharge, final int resultCode, final boolean success) {
        DependencyPayload payload = new DependencyPayload(name, data, Duration.ofMillis(timeTakenInMs), requestCharge, String.valueOf(resultCode), success);
        payload.setType("CosmosStore");
        payload.setTarget(target);

        CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).logDependency(payload);
    }

    /**
     * Return a string composed of partition ID, database name and collection.
     *
     * @param partitionId  the data partition ID
     * @param databaseName the Cosmos database name
     * @param collection   the Cosmos collection name
     * @return the dependency target string
     */
    public String getDependencyTarget(final String partitionId, final String databaseName, final String collection) {
        return String.format("%s:%s/%s", partitionId, databaseName, collection);
    }

    /**
     * Return a string composed of database name and collection.
     *
     * @param databaseName the Cosmos database name
     * @param collection   the Cosmos collection name
     * @return the dependency target string
     */
    public String getDependencyTarget(final String databaseName, final String collection) {
        return String.format("%s/%s", databaseName, collection);
    }
}
