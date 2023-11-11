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

package io.innospots.libra.kernel.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.configuration.InnospotsConsoleProperties;
import io.innospots.libra.kernel.service.TestService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;


/**
 * @author Alfred
 * @date 2021-08-26
 */
@Slf4j
@RestController
@RequestMapping(PATH_ROOT_ADMIN)
@Tag(name = "Test Case")
public class TestController {

    private InnospotsConsoleProperties innospotsConsoleProperties;

    private ObjectMapper jsonMapper;

    public TestService testService;

    public TestController(InnospotsConsoleProperties innospotsConsoleProperties, ObjectMapper jsonMapper, TestService testService) {
        this.innospotsConsoleProperties = innospotsConsoleProperties;
        this.jsonMapper = jsonMapper;
        this.testService = testService;
    }


    @GetMapping("jar/loader")
    @Operation(summary = "jar loader")
    public InnospotResponse<Boolean> LibLoader(@Parameter(name = "jarName") @RequestParam(value = "jarName", required = false, defaultValue = "innospots-extension-apps-1.0.0-SNAPSHOT.jar") String jarName) {

//        String jarUrl = "file://" + innospotsConfigProperties.getExtLibPath() + jarName;
//        ClassJarFileLoader.loadJar(jarUrl);
        return InnospotResponse.success(true);
    }

    @GetMapping("json")
    public InnospotResponse<Map<String, Object>> json() throws JsonProcessingException {
        Map<String, Object> m = new HashMap<>();
        m.put("time", LocalDateTime.now());
        m.put("date", LocalDate.now());
        m.put("json", jsonMapper.writeValueAsString(m));
        testService.log1(m);
        return InnospotResponse.success(m);
    }

    @PostMapping("test/echo")
    public InnospotResponse<Map<String, Object>> echo(
            @RequestBody Map<String,Object> body,
            @RequestParam Map<String,Object> params){
        Map<String, Object> m = new HashMap<>();
        m.put("body",body);
        m.put("params", params);
        return InnospotResponse.success(m);
    }
}
