package org.opengroup.osdu.azure.filters;

import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * MDC Filter for logging.
 */
@Component
@ConditionalOnProperty(value = "logging.mdccontext.enabled", havingValue = "true", matchIfMissing = false)
public class Slf4jMDCFilter implements Filter {
    @Autowired
    private DpsHeaders dpsHeaders;

    /**
     * Filter logic.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param filterChain Filter Chain object.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        MDC.setContextMap(getContextMap());
        filterChain.doFilter(servletRequest, servletResponse);
        MDC.clear();
    }

    /**
     * Method to create context map for mdc.
     * @return Context map.
     */
    private Map<String, String> getContextMap() {
        final Map<String, String> contextMap = new HashMap<>();
        contextMap.put(DpsHeaders.CORRELATION_ID, dpsHeaders.getCorrelationId());
        contextMap.put(DpsHeaders.DATA_PARTITION_ID, dpsHeaders.getPartitionId());
        return contextMap;
    }
}
