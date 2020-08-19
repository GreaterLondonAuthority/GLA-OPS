/**
 * Copyright (c) Greater London Authority, 2016.
 *
 * This source code is licensed under the Open Government Licence 3.0.
 *
 * http://www.nationalarchives.gov.uk/doc/open-government-licence/version/3/
 */
package uk.gov.london.ops.framework.filter;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Filter to allow caching of font files to work around a bug in IE11, when using https. See GLA-2598.
 *
 * Created by chris on 23/11/2016.
 */
@WebFilter(filterName = "FontCacheFilter", urlPatterns = {"*.woff", "*.woff2", "*.eot", "*.ttf", "*.svg"})
public class FontCacheControlFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) {
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        httpServletResponse.setHeader("Cache-Control","public, immutable");
        httpServletResponse.setHeader("Pragma","cache"); // is probably redundant.
        filterChain.doFilter(servletRequest, servletResponse);
    }

    @Override
    public void destroy() {

    }
}
