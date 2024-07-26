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
@ConditionalOnProperty(value = "log.sampling.enabled", havingValue = "true")
public class LogSamplerConfiguration {

    @Value("${log.sampling.info:100}")
    private int infoSamplingPercentage;

    @Value("${log.sampling.dependency:100}")
    private int dependencySamplingPercentage;
}
