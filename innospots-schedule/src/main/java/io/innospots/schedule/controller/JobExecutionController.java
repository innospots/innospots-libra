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
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.R;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.schedule.model.ExecutionFormQuery;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/6
 */
@RestController
@RequestMapping(PathConstant.ROOT_PATH +"schedule/job-execution")
@ModuleMenu(menuKey = "job-schedule")
@Tag(name = "schedule job execution")
public class JobExecutionController {

    private JobExecutionOperator jobExecutionOperator;

    public JobExecutionController(JobExecutionOperator jobExecutionOperator) {
        this.jobExecutionOperator = jobExecutionOperator;
    }

    @GetMapping("page")
    @Operation(summary = "page job executions")
    public R<PageBody<JobExecution>> pageJobExecutions(
            ExecutionFormQuery executionFormQuery) {
        return R.success(jobExecutionOperator.
                pageJobExecutions(executionFormQuery));
    }

    @Operation(summary = "get job execution")
    @GetMapping("{jobExecutionId}")
    public R<JobExecution> getJobExecution(@PathVariable String jobExecutionId,
                                           @RequestParam(required = false,defaultValue = "true") boolean includeSub) {
        return R.success(jobExecutionOperator.getJobExecution(jobExecutionId, includeSub));
    }

    @Operation(summary = "update job execution status")
    @PutMapping("{jobExecutionId}/status/{status}")
    public R<Boolean> updateStatus(@PathVariable String jobExecutionId,
                                   @PathVariable ExecutionStatus status,
                                   @RequestParam(required = false) String message) {
        return R.success(jobExecutionOperator.updateStatus(jobExecutionId,
                status, message));
    }

}
