package org.opengroup.osdu.azure.di;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * EventGridTopic settings.
 */
@Configuration
@ConfigurationProperties("azure.eventgrid.topic")
@Getter
@Setter
public class EventGridTopicRetryConfiguration {

    private static final int DEFAULT_INT_VALUE = -1;

    private int longRunningOperationRetryTimeout = DEFAULT_INT_VALUE;

    /**
     * Used to check if timeout is configured in application.properties.
     * @return True if retry is configured.
     */
    public boolean isTimeoutConfigured() {
        if (this.longRunningOperationRetryTimeout != DEFAULT_INT_VALUE) {
            return true;
        }
        return false;
    }
}
