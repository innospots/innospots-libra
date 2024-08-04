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
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringEscapeUtils;
import org.apache.hc.core5.http.HttpHeaders;

/**
 * @author Smars
 * @date 2021/2/15
 */
public class InnospotsHttpServletRequestWrapper extends HttpServletRequestWrapper {

    private static final String PARAM_REDIRECTS = "loadType";

    /**
     * not require authentication
     */
    private boolean ignoreAuth;

    /**
     * require authentication, but not require any roles permission
     */
    private boolean ignorePermission;

    /**
     * image, css, js
     */
    private boolean staticResource;

    /**
     * the path of application request, which the path start with '/apps/'
     */
    private boolean appPath;

    /**
     * innospots console controller api path, @See io.innospots.base.constant.PathConstant.ROOT_PATH
     */
    private boolean apiPath;

    public InnospotsHttpServletRequestWrapper(HttpServletRequest request) {
        super(request);
    }

    @Override
    public String getQueryString() {
        return StringEscapeUtils.escapeHtml4(super.getQueryString());
    }

    @Override
    public String getParameter(String name) {
        return StringEscapeUtils.escapeHtml4(super.getParameter(name));
    }

    @Override
    public String[] getParameterValues(String name) {
        String[] values = super.getParameterValues(name);
        if (ArrayUtils.isEmpty(values)) {
            return values;
        }
        int length = values.length;
        String[] escapeValues = new String[length];
        for (int i = 0; i < length; i++) {
            escapeValues[i] = StringEscapeUtils.escapeHtml4(values[i]);
        }
        return escapeValues;
    }


    public String redirectUrl() {
        String query = this.getQueryString();
        String servletPath = this.getServletPath();
        String redirectUrl = null;
        String loadType = this.getParameter(PARAM_REDIRECTS);

        if (appPath) {
            if ("page".equals(loadType)) {
                redirectUrl = "/redirect/index.html?target=" + servletPath;
            }
        }

        if (redirectUrl == null) {
            redirectUrl = fillIndex(servletPath, query);
        }
        /*
        if(redirectUrl== null && this.isAppEntryPage()){
            redirectUrl = "/redirect/index.html?target=" + servletPath;
        }else{
            redirectUrl = fillIndex(servletPath,query);
        }

         */
        return redirectUrl;
    }

    private String fillIndex(String redirectUrl, String query) {
        if (!redirectUrl.endsWith("index.html")) {
            redirectUrl += redirectUrl.endsWith("/") ? "" : "/";
            redirectUrl += "index.html";
        }
        if (StringUtils.isNotEmpty(query)) {
            redirectUrl += "?" + query;
        }
        return redirectUrl;
    }


    /**
     * the application entry page request
     *
     * @return
     */
    private boolean isAppEntryPage() {
        if (!appPath) {
            return false;
        }
        String dest = this.getHeader("Sec-Fetch-Dest");
        if (dest != null) {
            if (dest.contains("document")) {
                return true;
            } else {
                return false;
            }
        }
        String mode = this.getHeader("Sec-Fetch-Mode");
        if (mode != null) {
            if (mode.contentEquals("navigate")) {
                return true;
            } else {
                return false;
            }
        }

        return notAppPageRefer();
    }

    private boolean notAppPageRefer() {
        String referer = this.getHeader(HttpHeaders.REFERER);
        if (referer == null || !referer.contains(PathConstant.APP_PATH)) {
            return true;
        }
        return false;
    }


    public boolean isIgnoreAuth() {
        return ignoreAuth;
    }

    public void setIgnoreAuth(boolean ignoreAuth) {
        this.ignoreAuth = ignoreAuth;
    }

    public boolean isIgnorePermission() {
        return ignorePermission;
    }

    public void setIgnorePermission(boolean ignorePermission) {
        this.ignorePermission = ignorePermission;
    }

    public boolean isStaticResource() {
        return staticResource;
    }

    public void setStaticResource(boolean staticResource) {
        this.staticResource = staticResource;
    }

    public boolean isApiPath() {
        return apiPath;
    }

    public void setApiPath(boolean apiPath) {
        this.apiPath = apiPath;
    }

    public boolean isAppPath() {
        return appPath;
    }

    public void setAppPath(boolean appPath) {
        this.appPath = appPath;
    }
}
