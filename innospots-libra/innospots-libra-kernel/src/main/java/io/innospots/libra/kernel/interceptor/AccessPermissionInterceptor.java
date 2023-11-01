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

import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.CCH;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.filter.InnospotsHttpServletRequestWrapper;
import io.innospots.libra.base.menu.ModuleMenuOperation;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import io.innospots.libra.kernel.module.system.service.UserResourceService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletRequestWrapper;
import javax.servlet.http.HttpServletResponse;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;


/**
 * interceptor menu resource
 *
 * @author Smars
 * @date 2021/2/16
 */
@Slf4j
public class AccessPermissionInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(AccessPermissionInterceptor.class);

    private UserResourceService userResourceService;

    private AuthProperties authProperties;

    private List<ResourceRequestMatcher> resourceRequestMatchers = new CopyOnWriteArrayList<>();

    public AccessPermissionInterceptor() {
        initialize();
    }

    private void initialize() {
        Collection<ResourceItem> resourceItems = LibraClassPathExtPropertiesLoader.getAllResourceItems();
        fillMatcher(resourceItems);
    }

    private void fillMatcher(Collection<ResourceItem> resourceItems) {
        for (ResourceItem resourceItem : resourceItems) {
            if (log.isDebugEnabled()) {
                log.debug("build matcher:{}", resourceItem);
            }
            resourceRequestMatchers.add(new ResourceRequestMatcher(resourceItem));
            if (CollectionUtils.isNotEmpty(resourceItem.getItems())) {
                fillMatcher(resourceItem.getItems());
            }
        }//end for
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {

        if (request instanceof InnospotsHttpServletRequestWrapper) {
            InnospotsHttpServletRequestWrapper servletRequestWrapper = (InnospotsHttpServletRequestWrapper) request;

            if (servletRequestWrapper.isIgnoreAuth() || servletRequestWrapper.isIgnorePermission()) {
                return true;
            }
        } else if (request instanceof HttpServletRequestWrapper && ((HttpServletRequestWrapper) request).getRequest() instanceof InnospotsHttpServletRequestWrapper) {
            InnospotsHttpServletRequestWrapper servletRequestWrapper = (InnospotsHttpServletRequestWrapper) ((HttpServletRequestWrapper) request).getRequest();
            if (servletRequestWrapper.isIgnoreAuth() || servletRequestWrapper.isIgnorePermission()) {
                return true;
            }
        }


        Integer userId = CCH.userId();
        if (userId <= 0) {
            throw AuthenticationException.buildUserException(this.getClass(), "Invalid User");
        }

        boolean permission = hasPermission(request);
        if (!permission) {
            log.warn("user: {} not have permission, path:{}", CCH.authUser(), request.getServletPath());
            throw AuthenticationException.buildPermissionException(this.getClass(), CCH.authUser(), request.getServletPath());
        }
        return permission;
    }

    /**
     * get optElement
     *
     * @param request
     * @return
     */
    private boolean hasPermission(HttpServletRequest request) {
        ResourceRequestMatcher matcher = null;
        ModuleMenuOperation menuOperation = null;
        for (ResourceRequestMatcher requestMatcher : resourceRequestMatchers) {
            ModuleMenuOperation moduleMenuOperation = requestMatcher.matchOperationItem(request);
            if (moduleMenuOperation != null) {
                matcher = requestMatcher;
                menuOperation = moduleMenuOperation;
                break;
            }
        }

        boolean adminRole = isSuperAdminRole();

        if (adminRole && authProperties.isOpenSuperAdminPermission()) {
            //open suer admin
            return true;
        }

        String itemKey = null;

        if (!adminRole) {
            adminRole = userResourceService.isProjectAdminRole();
        }

        if (matcher != null) {
            if (adminRole && matcher.isAdminResource()) {
                return true;
            }

            //controller method not have ResourceItemOperation annotation ,if optElement == null
            OptElement optElement = menuOperation.findOptElement(request);
            if (optElement != null) {
                itemKey = optElement.getItemKey();
            }

            if (itemKey == null) {
                if (!HttpMethod.GET.name().equalsIgnoreCase(request.getMethod())) {
                    logger.warn("{} path not have optElement define: {}, using controller ResourceKey:{}", request.getMethod(), request.getServletPath(), matcher.getResourceItemKey());
                }
                itemKey = matcher.getResourceItemKey();
            }

        }

        if (itemKey == null) {
            logger.warn("{} path not have optElement define:{}", request.getMethod(), request.getServletPath());
            //throw AuthenticationException.buildPermissionException(this.getClass(),CCH.authUser(),request.getServletPath());
            return true;
        }

        return userResourceService.hasResource(CCH.userId(), itemKey);

    }

    protected boolean hasPagePermission(String pageUrl) {

        boolean adminRole = isSuperAdminRole();

        if (adminRole && authProperties.isOpenSuperAdminPermission()) {
            //open suer admin
            return true;
        }

        String itemKey = null;
        for (ResourceRequestMatcher requestMatcher : resourceRequestMatchers) {
            boolean isMatched = requestMatcher.matchResource(pageUrl);
            if (isMatched) {
                itemKey = requestMatcher.getResourceItemKey();
                break;
            }
        }//end for

        if (itemKey == null) {
            log.warn("page url not match resource, user:{} ,page: {}", CCH.userId(), pageUrl);
            return false;
        }
        boolean pagePermission = userResourceService.hasResource(CCH.userId(), itemKey);
        if (!pagePermission) {
            log.warn("page url not allow access, user:{} ,page: {}", CCH.userId(), pageUrl);
        }
        return pagePermission;
    }

    private boolean isSuperAdminRole() {
        if (userResourceService == null) {
            userResourceService = BeanContextAwareUtils.getBean(UserResourceService.class);
        }
        if (authProperties == null) {
            authProperties = BeanContextAwareUtils.getBean(AuthProperties.class);
        }

        return userResourceService.isSuperAdminRole();
    }
}