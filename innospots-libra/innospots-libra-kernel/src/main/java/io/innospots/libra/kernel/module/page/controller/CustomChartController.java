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

package io.innospots.libra.kernel.module.page.controller;

import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.response.InnospotResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2023/1/24
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "plugins")
@Tag(name = "CustomChart")
public class CustomChartController {

    private static final String CUSTOM_CHART_PLUGINS = "custom-chart-plugins";

    @GetMapping("custom/charts")
    @Operation(summary = "custom charts")
    public InnospotResponse<Set<String>> listCustomChart() {
        Set<String> set = new HashSet<>();
        try {
            Resource[] resources = new PathMatchingResourcePatternResolver()
                    .getResources("classpath*:META-INF/resources/apps/visualization/" + CUSTOM_CHART_PLUGINS + "/*.*");
            for (Resource resource : resources) {
                set.add(CUSTOM_CHART_PLUGINS + "/" + resource.getFilename());
            }
        } catch (IOException e) {
            throw ResourceException.buildNotExistException(this.getClass());
        }
        return InnospotResponse.success(set);
    }
}
