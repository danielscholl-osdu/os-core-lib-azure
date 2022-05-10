package org.opengroup.osdu.azure.filters;

import com.nimbusds.jwt.JWTClaimsSet;
import org.opengroup.osdu.azure.util.AuthUtils;
import org.opengroup.osdu.core.common.model.http.DpsHeaders;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.beans.factory.BeanFactoryUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.mvc.method.RequestMappingInfo;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.servlet.Filter;
import javax.servlet.ServletContext;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * MDC Filter for logging.
 * Condition 1: MDC filter should be called before any filter which contains logging logic, like TransactionLogFilter
 * Condition 2: MDC filter should be called after OrderedRequestContextFilter, so that the request scope is set
 * Filter with lowest order value is picked up first
 * The order of TransactionLogFilter is not set, hence it picks up default value which is Ordered.LOWEST_PRECEDENCE (2147483647)
 * The order of OrderedRequestContextFilter is -105
 * Hence setting the order of MDC filter as -104
 * So now order of calling the filters becomes: OrderedRequestContextFilter --> MDC filter --> ... --> TransactionLogFilter
 */
@Component
@ConditionalOnProperty(value = "logging.mdccontext.enabled", havingValue = "true", matchIfMissing = true)
@Order(-104)
public class Slf4jMDCFilter implements Filter {

    private static final Logger LOGGER = LoggerFactory.getLogger(Slf4jMDCFilter.class);
    @Autowired
    private DpsHeaders dpsHeaders;
    @Autowired
    private AuthUtils authUtils;

    /**
     * Filter logic.
     *
     * @param servletRequest  Request object.
     * @param servletResponse Response object.
     * @param filterChain     Filter Chain object.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse,
                         final FilterChain filterChain) throws IOException, ServletException {
        MDC.clear();
        MDC.setContextMap(getContextMap(servletRequest));
        /* fix for api operation name */
        filterChain.doFilter(servletRequest, servletResponse);
    }

    /**
     * Method to create context map for mdc.
     * @param servletRequest Response Object
     * @return Context map.
     */
    private Map<String, String> getContextMap(final ServletRequest servletRequest)  {
        final Map<String, String> contextMap = new HashMap<>();
        contextMap.put(DpsHeaders.CORRELATION_ID, dpsHeaders.getCorrelationId());
        contextMap.put(DpsHeaders.DATA_PARTITION_ID, dpsHeaders.getPartitionId());

        //Adding custom columns for business metric

        String operationApiPath = getOperationName(servletRequest);
        contextMap.put("api-method", ((HttpServletRequest) servletRequest).getMethod());
        contextMap.put("operation-name", operationApiPath);

        if (dpsHeaders.getAppId() != null) {
            contextMap.put("app-id", dpsHeaders.getAppId());
        }

        String userId = getUserId();
        if (userId != null) {
            contextMap.put("user-id", userId);
        }
        return contextMap;
    }

    /**
     * Get user ID from Authorization payload (JWT token).
     *
     * @return the user ID
     */
    private String getUserId() {
        JWTClaimsSet claimsSet = authUtils.getClaimsFromJwtToken(dpsHeaders.getAuthorization());
        return claimsSet == null ? null : claimsSet.getSubject();
    }


    /**
     * Method to create context map for mdc.
     * @param servletRequest Response Object
     * @return OperationApiPath name
     */
    private String getOperationName(final ServletRequest servletRequest) {

        String operationApiPath = "";
        ServletContext servletContext = ((HttpServletRequest) servletRequest).getSession().getServletContext();
        WebApplicationContext appContext = WebApplicationContextUtils.getWebApplicationContext(servletContext);

        if (appContext != null) {
            Map<String, HandlerMapping> allRequestMappings = BeanFactoryUtils.beansOfTypeIncludingAncestors(appContext, HandlerMapping.class, true, false);
            HandlerMapping handlerMapping = allRequestMappings.get("requestMappingHandlerMapping");
            RequestMappingHandlerMapping requestMappingHandlerMapping = (RequestMappingHandlerMapping) handlerMapping;
            Map<RequestMappingInfo, HandlerMethod> handlerMethods = requestMappingHandlerMapping.getHandlerMethods();
            /* Swapping key value pair */
            Map<String, String> handlerApiMap = new HashMap<>();
            for (Map.Entry<RequestMappingInfo, HandlerMethod> entry : handlerMethods.entrySet()) {
                handlerApiMap.put(entry.getValue().toString(), entry.getKey().toString());
            }

            try {
                operationApiPath = handlerApiMap.get(Objects.requireNonNull(requestMappingHandlerMapping.getHandler((HttpServletRequest) servletRequest)).getHandler().toString());
            } catch (Exception e) {
                LOGGER.warn("Unable to get the operation-name  due to {}", e.getMessage(), e);
            }
        }
        return operationApiPath;
    }
}

