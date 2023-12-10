/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.schedule.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.schedule.launcher.JobExecutor;
import io.innospots.schedule.model.ReadyJob;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH +"job")
public class JobExecutorController {

    private JobExecutor jobExecutor;

    @PostMapping("execute/{jobKey}")
    public InnospotResponse<ReadyJob> launch(@PathVariable String jobKey, @RequestBody Map<String,Object> params){
        ReadyJob readyJob = jobExecutor.execute(jobKey,params);
        return InnospotResponse.success(readyJob);
    }

}
