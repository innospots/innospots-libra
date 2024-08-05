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

package io.innospots.libra.security.filter;


import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.filter.InnospotsHttpServletRequestWrapper;
import io.innospots.libra.security.context.SecurityContextHolder;
import io.innospots.libra.security.jwt.JwtAuthManager;
import io.innospots.libra.security.jwt.JwtToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


/**
 * @author Smars
 * @date 2021/2/16
 */
public class AuthenticationFilter implements Filter {

    private static final Logger logger = LoggerFactory.getLogger(AuthenticationFilter.class);


    private final JwtAuthManager jwtAuthManager;

    public AuthenticationFilter(JwtAuthManager jwtAuthManager) {
        this.jwtAuthManager = jwtAuthManager;
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        InnospotsHttpServletRequestWrapper httpServletRequest = (InnospotsHttpServletRequestWrapper) servletRequest;

        //not console api or ignore
        if (!httpServletRequest.isApiPath() || httpServletRequest.isIgnoreAuth()) {
            filterChain.doFilter(httpServletRequest, servletResponse);
            return;
        }

        boolean result;
        try {
            result = authToken(servletRequest);
        } catch (AuthenticationException e) {
            this.writeErrorResponse((HttpServletResponse) servletResponse, ResponseCode.AUTH_TOKEN_INVALID, ResponseCode.AUTH_TOKEN_INVALID.getInfo());
            return;

        } catch (Exception e) {
            this.writeErrorResponse((HttpServletResponse) servletResponse, ResponseCode.AUTH_TOKEN_EXPIRED, ResponseCode.AUTH_TOKEN_EXPIRED.getInfo());
            return;
        }
        if (result) {
            filterChain.doFilter(servletRequest, servletResponse);
        }
    }

    private boolean authToken(ServletRequest servletRequest) throws AuthenticationException {
        JwtToken jwtToken = jwtAuthManager.validToken((HttpServletRequest) servletRequest);
        servletRequest.setAttribute("token", jwtToken);
        CCH.register(jwtToken.getOrgId(), jwtToken.getProjectId(), jwtToken.getUserName(), jwtToken.getUserId());
        SecurityContextHolder.setContext(jwtToken);
        return authWebToken(jwtToken);
    }

    private boolean authWebToken(JwtToken jwtToken) {
        return jwtToken.isTokenNotExpired();
    }

    private void writeErrorResponse(HttpServletResponse response, ResponseCode code, String message) {
        response.setContentType("text/json; charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_OK);
        try {
            PrintWriter writer = response.getWriter();
            String rtn = JSONUtils.toJsonString(InnospotsResponse.fail(code, message));
            writer.write(rtn);
            writer.close();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
