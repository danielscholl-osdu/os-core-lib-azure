package org.opengroup.osdu.azure.util;

import com.azure.cosmos.implementation.RequestRateTooLargeException;
import org.opengroup.osdu.core.common.model.http.AppException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

/**
 * Azure as service specific resource exception handler.
 */
@ControllerAdvice
public final class GlobalAzureExceptionMapper {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalAzureExceptionMapper.class);

    /**
     * RequestRateTooLargeException handler.
     *
     * @param e RequestRateTooLargeException exception
     * @return SERVICE_UNAVAILABLE response
     */
    @ExceptionHandler(RequestRateTooLargeException.class)
    protected ResponseEntity<Object> handleCosmosdbException(final RequestRateTooLargeException e) {
        return this.getErrorResponse(
                new AppException(HttpStatus.SERVICE_UNAVAILABLE.value(), "Service Unavailable",
                        "Request rate is large. Please retry this request later", e));
    }

    /**
     * @param e AppException exception
     * @return Erroneous response
     */
    private ResponseEntity<Object> getErrorResponse(final AppException e) {

        String exceptionMsg = e.getOriginalException() != null
                ? e.getOriginalException().getMessage()
                : e.getError().getMessage();

        if (e.getError().getCode() > 499) {
            this.LOGGER.error(exceptionMsg, e);
        } else {
            this.LOGGER.warn(exceptionMsg, e);
        }

        return new ResponseEntity<Object>(e.getError(), HttpStatus.resolve(e.getError().getCode()));
    }
}
