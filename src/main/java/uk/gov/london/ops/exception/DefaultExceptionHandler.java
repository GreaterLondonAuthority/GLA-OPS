/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.exception;

import org.hibernate.StaleStateException;
import org.jboss.logging.MDC;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
public class DefaultExceptionHandler {

    Logger log = LoggerFactory.getLogger(getClass());

    @ExceptionHandler(value = NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public void handleException(NotFoundException e) {}

    @ExceptionHandler(value = ValidationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ApiError handleException(HttpServletRequest request, ValidationException e) {
        logError(request, e.getError().getId(), e);
        return e.getError();
    }

    /**
     * We dont want to catch instances of these spring exceptions but let them be handled by the framework.
     * @param e
     */
    @ExceptionHandler(value = {AuthenticationException.class, AccessDeniedException.class})
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public void handleException(RuntimeException e) {
        throw e;
    }

    @ExceptionHandler(value = ForbiddenAccessException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleException(ForbiddenAccessException e) {}

    @ExceptionHandler(value = StaleStateException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public void handleException(StaleStateException e) {}

    @ExceptionHandler(value = Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ApiError handleException(HttpServletRequest request, Exception e) {
        String message = "Sorry, an unexpected error occurred";
        if (e.getMessage() != null && !e.getMessage().isEmpty()) {
            message += " : "+e.getMessage();
        }
        ApiError apiError = new ApiError(message);
        logError(request, apiError.getId(), e);
        return apiError;
    }

    void logError(HttpServletRequest request, String errorId, Exception e) {
        Authentication authContext = SecurityContextHolder.getContext().getAuthentication();
        String user = authContext == null ? "anonymous" : authContext.getName();
        String endpoint = "unknown";
        if (request != null) {
            endpoint = request.getMethod()+" "+request.getRequestURI();
        }
        String message = String.format("error id: %s, user: %s, endpoint: %s, message: %s", errorId, user, endpoint, e.getMessage());
        MDC.put("errorId", errorId);
        if (e instanceof ValidationException) {
            log.debug(message, e);
        }
        else {
            log.error(message, e);
        }
        MDC.remove("errorId");
    }

}
