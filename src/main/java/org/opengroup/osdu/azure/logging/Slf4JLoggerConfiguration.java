package org.opengroup.osdu.azure.logging;

import org.opengroup.osdu.core.common.model.http.HeadersToLog;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;

/**
 * Creating beans needed for Slf4JLogger.
 */
@Configuration
public class Slf4JLoggerConfiguration {
    /**
     * Bean for HeadersToLog used in {@link Slf4JLogger}.
     * @return {@link HeadersToLog} instance
     */
    @Bean
    public HeadersToLog headersToLog() {
        return new HeadersToLog(Collections.emptyList());
    }
}
