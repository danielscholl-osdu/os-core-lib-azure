package org.opengroup.osdu.azure.di;


import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * EventGridTopic settings.
 */
@Configuration
@ConfigurationProperties("azure.eventgridtopic")
@Getter
@Setter
public class EventGridTopicRetryConfiguration {

    private int longRunningOperationRetryTimeout = -1;

    /**
     * Used to check if timeout is configured in application.properties.
     * @return True if retry is configured.
     */
    public boolean isTimeoutConfigured() {
        if (this.longRunningOperationRetryTimeout != -1) {
            return true;
        }
        return false;
    }
}
