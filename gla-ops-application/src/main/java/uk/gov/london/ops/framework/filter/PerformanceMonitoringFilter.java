/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.filter;

import java.io.IOException;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;

@WebFilter(filterName = "PerformanceMonitoringFilter", urlPatterns = {"/*"})
public class PerformanceMonitoringFilter implements Filter {

    Logger log = LoggerFactory.getLogger(getClass());

    @Value("${service.execution.time.threshold}")
    long threshold = 1000;

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        long before = System.currentTimeMillis();
        chain.doFilter(request, response);
        long after = System.currentTimeMillis();
        long executionTime = after - before;
        if (executionTime > threshold) {
            HttpServletRequest httpServletRequest = (HttpServletRequest) request;
            log.warn("{} execution time {}ms", httpServletRequest.getMethod() + " " + httpServletRequest.getRequestURI(),
                    executionTime);
        }
    }

    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void destroy() {
    }

}
