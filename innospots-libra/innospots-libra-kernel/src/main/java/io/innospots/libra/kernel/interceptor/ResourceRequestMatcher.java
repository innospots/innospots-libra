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

import io.innospots.base.model.Pair;
import io.innospots.libra.base.matcher.UrlPathRequestMatcher;
import io.innospots.libra.base.menu.ModuleMenuOperation;
import io.innospots.libra.base.menu.OptElement;
import io.innospots.libra.base.menu.ResourceItem;
import org.apache.commons.collections4.CollectionUtils;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/7/19
 */
public class ResourceRequestMatcher {


    private final ResourceItem resourceItem;

    private List<Pair<UrlPathRequestMatcher, OptElement>> urlMatchers = new ArrayList<>();

    public ResourceRequestMatcher(ResourceItem resourceItem) {
        this.resourceItem = resourceItem;
        //constructUrlMatcher(resourceItem.getOpts());
    }

    public boolean isAdminResource() {
        return resourceItem.isAdminDefault();
    }

    public ModuleMenuOperation matchOperationItem(HttpServletRequest request) {
        if (request.getServletPath() == null) {
            return null;
        }
        for (ModuleMenuOperation moduleMenuOperation : resourceItem.getModuleMenuOperations()) {
            if (moduleMenuOperation.getPath() == null) {
                continue;
            }
            if (request.getServletPath().startsWith(moduleMenuOperation.getPath())) {
                return moduleMenuOperation;
            }
        }//end for
        return null;
//        return request.getServletPath().startsWith(resourceItem.getPath());
    }

    public String getResourceItemKey() {
        return resourceItem.getItemKey();
    }


    /**
     * Construct URL Matcher
     *
     * @param optElements
     */
    private void constructUrlMatcher(List<OptElement> optElements) {
        if (CollectionUtils.isNotEmpty(optElements)) {
            for (OptElement element : optElements) {
                String uri = element.getUri();
                if (uri.contains("{") && uri.contains("}")) {
                    String pattern = "\\{[^}]+}";
                    Pattern r = Pattern.compile(pattern);
                    Matcher m = r.matcher(uri);
                    while (m.find()) {
                        uri = uri.replace(m.group(), "**");
                    }
                }
                urlMatchers.add(Pair.of(new UrlPathRequestMatcher(Collections.singletonList(uri)), element));
            }
        }
    }

    /**
     * get optElement
     *
     * @param request
     * @return
     */
    public OptElement findOptElement(HttpServletRequest request) {
        if (CollectionUtils.isEmpty(urlMatchers)) {
            return null;
        }
        for (Pair<UrlPathRequestMatcher, OptElement> pair : urlMatchers) {
            if (request.getMethod().equalsIgnoreCase(pair.getRight().getMethod().name()) && pair.getLeft().matches(request)) {
                return pair.getRight();
            }
        }
        return null;
    }

    public boolean matchResource(String pageUrl) {
        return resourceItem.matchPageUrl(pageUrl);
    }

}
