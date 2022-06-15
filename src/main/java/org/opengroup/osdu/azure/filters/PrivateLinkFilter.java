package org.opengroup.osdu.azure.filters;

import org.apache.commons.validator.routines.InetAddressValidator;
import org.opengroup.osdu.azure.privateLinks.ValidateDataLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.validation.ValidationException;
import java.io.IOException;


@Component
@ConditionalOnProperty(value = "validate.privateLink.enabled", havingValue = "true", matchIfMissing = true)
@Order(-103)
public class PrivateLinkFilter implements Filter {

    @Autowired
    ValidateDataLinks validateDataLinks;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        // get the ip address

        // https://mkyong.com/java/how-to-get-client-ip-address-in-java/ ?

        String ipAddress = getIpaddress(servletRequest);
        InetAddressValidator inetAddressValidator =  InetAddressValidator.getInstance();

        if(inetAddressValidator.isValidInet6Address(ipAddress)) {

            //ipv4 vs ipv6 -> only for ipv6
            if (validateDataLinks.validateRequest(ipAddress))
                filterChain.doFilter(servletRequest, servletResponse);
            else {
                throw new ValidationException("Validation of data link failed.");
            }
        }
        else
            filterChain.doFilter(servletRequest, servletResponse);

    }

    public String getIpaddress(ServletRequest servletRequest){

        HttpServletRequest request = (HttpServletRequest)servletRequest;

        String ipAddress ="";
        if (request != null) {
            ipAddress = request.getHeader("X-FORWARDED-FOR");
            if (ipAddress == null || "".equals(ipAddress)) {
                ipAddress = request.getRemoteAddr();
            }
        }

        return ipAddress;
    }
}
