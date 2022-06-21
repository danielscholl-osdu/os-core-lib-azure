package org.opengroup.osdu.azure.filters;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.opengroup.osdu.azure.privateLinks.ValidateDataLinks;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.FilterChain;
import javax.servlet.Filter;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;


/**
    This filter is for validation of private links.
    Order of calling the filters becomes: OrderedRequestContextFilter --> MDC filter -->PrivateLinkFilter --> ... --> TransactionLogFilter
 */
@Component
@ConditionalOnProperty(value = "validate.privateLink.enabled", havingValue = "true", matchIfMissing = true)
@Order(-103)
public class PrivateLinkFilter implements Filter {


    private static final Logger LOGGER = LoggerFactory.getLogger(PrivateLinkFilter.class);
    @Autowired
    private ValidateDataLinks validateDataLinks;


    /**
     * Filter logic.
     * @param servletRequest Request object.
     * @param servletResponse Response object.
     * @param filterChain Filter Chain object.
     * @throws IOException
     * @throws ServletException
     */
    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        // get the ip address

        LOGGER.info("Getting the ipaddress");
        String ipAddress = getIpaddress(servletRequest);
        InetAddressValidator inetAddressValidator =  InetAddressValidator.getInstance();

        LOGGER.info("Validating the ipaddress");
        if (inetAddressValidator.isValidInet6Address(ipAddress)) {

            LOGGER.info("Ip address is ipv6");

            if (validateDataLinks.validateRequest(ipAddress)) {
                LOGGER.info("Validation is successful");
                filterChain.doFilter(servletRequest, servletResponse);
            } else {
                LOGGER.error("Validation error");
                throw new ValidationException("Validation of data link failed.");
            }
        } else {
            LOGGER.info("Ip address is ipv4");
            filterChain.doFilter(servletRequest, servletResponse);
        }

    }

    /**
        To get the ipAddress from Servlet request.
        * @param servletRequest ServletRequest Object
        * @return ipAddress String
     */

    public String getIpaddress(final ServletRequest servletRequest) {

        HttpServletRequest request = (HttpServletRequest) servletRequest;

        String ipAddress = "";
        if (request != null) {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || "".equals(ipAddress)) {

                if(request.getRemoteAddr().contains(","))
                    ipAddress = request.getRemoteAddr().split(",")[0];
                else
                    ipAddress =request.getRemoteAddr();
            }
        }
        return ipAddress;
    }
}
