package org.opengroup.osdu.azure.logging;

import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

/**
 * Log sampling configuration.
 */
@Configuration
@Getter
@ConditionalOnProperty(value = "logging.slf4jlogger.sampling.enabled", havingValue = "true")
public class LogSamplerConfiguration {

    @Value("${logging.slf4jlogger.sampling.info:100}")
    private int infoSamplingPercentage;

    @Value("${logging.slf4jlogger.sampling.dependency:100}")
    private int dependencySamplingPercentage;
}
