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

package io.innospots.workflow.core.instance.model;

import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.flow.model.WorkflowBaseInfo;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Raydian
 * @date 2021/1/18
 */
@Schema
@Getter
@Setter
public class WorkflowInstanceBase extends WorkflowBaseInfo {

    @Schema(title = "workflow instance primary id")
    protected Long workflowInstanceId;

    @Schema(title = "strategy status")
    protected DataStatus status;

    @Schema(title = "workflow instance revision")
    protected Integer revision;

    /**
     * datasource unique key
     */
    @Schema(title = "datasource that store the execution of workflow")
    protected String datasourceCode;

    @Schema(title = "workflow dashboard page primary id")
    protected Integer pageId;

    @Schema(title = "updated time")
    protected LocalDateTime updatedTime;

    @Schema(title = "created time")
    protected LocalDateTime onlineTime;


    public String identifier() {
        return IWorkflowLoader.key(workflowInstanceId, revision);
    }

    public String key() {
        return IWorkflowLoader.key(workflowInstanceId, revision);
    }
}
