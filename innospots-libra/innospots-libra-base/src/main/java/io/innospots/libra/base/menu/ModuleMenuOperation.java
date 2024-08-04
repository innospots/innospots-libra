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

package io.innospots.libra.base.menu;

import io.innospots.base.model.Pair;
import io.innospots.libra.base.matcher.UrlPathRequestMatcher;
import jakarta.servlet.http.HttpServletRequest;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/6/27
 */
public class ModuleMenuOperation {

    /**
     * controller mapping path
     */
    private String path;

    private String controllerKey;

    private ModuleMenu moduleMenu;

    private List<Pair<UrlPathRequestMatcher, OptElement>> urlMatchers = new ArrayList<>();

    private List<OptElement> optElements = new ArrayList<>();

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getControllerKey() {
        return controllerKey;
    }

    public void setControllerKey(String controllerKey) {
        this.controllerKey = controllerKey;
    }

    public ModuleMenu getModuleMenu() {
        return moduleMenu;
    }

    public void setModuleMenu(ModuleMenu moduleMenu) {
        this.moduleMenu = moduleMenu;
    }

    public List<OptElement> getOptElements() {
        return optElements;
    }

    public void build() {
        if (CollectionUtils.isEmpty(urlMatchers) && CollectionUtils.isNotEmpty(optElements)) {
            constructUrlMatcher(this.optElements);
        }
    }

    public void addOptElement(OptElement optElement) {
        optElements.add(optElement);
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

}
