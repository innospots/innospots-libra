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

package io.innospots.libra.kernel.interceptor;

import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/15
 */
public class PageAccessCheckInterceptor implements HandlerInterceptor {

    private final AccessPermissionInterceptor permissionInterceptor;

    private final Set<String> ignorePermissionPaths = new HashSet<>();

    public PageAccessCheckInterceptor(AccessPermissionInterceptor permissionInterceptor) {
        this.permissionInterceptor = permissionInterceptor;
//        ignorePermissionPaths.add("/");
        ignorePermissionPaths.add("/403");
        ignorePermissionPaths.add("/page/show");
        ignorePermissionPaths.add("/page/preview");
        ignorePermissionPaths.add("/apps/visualization");
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String pageUrl = request.getParameter("pageUrl");
        if (pageUrl == null || "/".equals(pageUrl)) {
            request.setAttribute("pageAccess", true);
            return true;
        }

        for (String ignorePermissionPath : ignorePermissionPaths) {
            if (pageUrl.startsWith(ignorePermissionPath)) {
                request.setAttribute("pageAccess", true);
                return true;
            }
        }

        Boolean pageAccess = this.permissionInterceptor.hasPagePermission(pageUrl);
        request.setAttribute("pageAccess", pageAccess);
        return true;
    }
}
