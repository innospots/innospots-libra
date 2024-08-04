/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.base.filter;


import io.innospots.base.constant.PathConstant;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.matcher.UrlPathRequestMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Smars
 * @date 2021/2/16
 */
public class IgnorePathFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(IgnorePathFilter.class);

    private final UrlPathRequestMatcher ignoreAuthPathRequestMatcher;

    private final UrlPathRequestMatcher ignorePermissionPathRequestMatcher;

    private final AuthProperties authProperties;


    public IgnorePathFilter(AuthProperties authProperties) {
        ignoreAuthPathRequestMatcher = new UrlPathRequestMatcher(authProperties.getIgnoreAuthPaths());
        ignorePermissionPathRequestMatcher = new UrlPathRequestMatcher(authProperties.getIgnorePermissionPaths());
        this.authProperties = authProperties;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String urlPath = httpServletRequest.getServletPath();

        InnospotsHttpServletRequestWrapper httpServletRequestWrapper = new InnospotsHttpServletRequestWrapper(httpServletRequest);

        if (urlPath != null) {
            httpServletRequestWrapper.setApiPath(urlPath.startsWith(PathConstant.ROOT_PATH));
            // image, css, js etc, which are static resources
            boolean staticResource = Arrays.stream(PathConstant.STATIC_RESOURCE_SUFFIX).anyMatch(urlPath::endsWith);
            httpServletRequestWrapper.setStaticResource(staticResource);
            httpServletRequestWrapper.setAppPath(urlPath.startsWith(PathConstant.APP_PATH));
        }

        // skipping authenticated token
        boolean skippedPath = ignoreAuthPathRequestMatcher.matches(httpServletRequest);
        httpServletRequestWrapper.setIgnoreAuth(skippedPath);
        if (!skippedPath) {
            // skipping authentication permission
            httpServletRequestWrapper.setIgnorePermission(ignorePermissionPathRequestMatcher.matches(httpServletRequest));
        }
        filterChain.doFilter(httpServletRequestWrapper, servletResponse);
    }
}