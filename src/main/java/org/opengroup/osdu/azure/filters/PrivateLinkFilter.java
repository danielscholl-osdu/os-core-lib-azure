package org.opengroup.osdu.azure.filters;

import org.opengroup.osdu.azure.privateLinks.ValidateDataLinks;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.servlet.*;
import javax.validation.ValidationException;
import java.io.IOException;

@Component
@ConditionalOnProperty(value = "validate.privateLink.enabled", havingValue = "true", matchIfMissing = true)
public class PrivateLinkFilter implements Filter {

    @Autowired
    ValidateDataLinks validateDataLinks;

    @Override
    public void doFilter(final ServletRequest servletRequest, final ServletResponse servletResponse, final FilterChain filterChain) throws IOException, ServletException {

        // get the ip address

        if(validateDataLinks.validateRequest(""))
            filterChain.doFilter(servletRequest,servletResponse);
        else{
            throw new ValidationException("Validation of data link failed.");
        }


    }
}
