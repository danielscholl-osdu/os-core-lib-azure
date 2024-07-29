package org.opengroup.osdu.azure.logging;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Random;

/**
 * Dependency logger.
 */
@Component
public class DependencyLogger {

    private static final String LOGGER_NAME = DependencyLogger.class.getName();

    private Random random = new Random();

    @Autowired(required = false)
    private LogSamplerConfiguration logSamplerConfiguration;

    /**
     * Log dependency with options.
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
        logDependencyWithPayload(payload);
    }

    /**
     * Log dependency with payload.
     * @param payload Dependency payload
     */
    public void logDependencyWithPayload(final DependencyPayload payload) {
        if (logSamplerConfiguration != null && payload.isSuccess() && getRandomNumberBetween1And100() > logSamplerConfiguration.getDependencySamplingPercentage()) {
            return;
        } else {
            CoreLoggerFactory.getInstance().getLogger(LOGGER_NAME).logDependency(payload);
        }
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

    /**
     * Returns a random number between 1 and 100, inclusive.
     * @return an int between 1 and 100, inclusive.
     */
    private int getRandomNumberBetween1And100() {
        return random.nextInt(100) + 1;
    }
}
