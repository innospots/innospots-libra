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

package io.innospots.workflow.core.flow.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;

/**
 * @author Raydian
 * @date 2021/1/18
 */
@Schema
@Getter
@Setter
public class WorkflowBaseInfo {

    @NotNull(message = "name must not be null")
    @Schema(title = "character alphabetic, number and underline characters")
    @Size(max = 32, message = "name is too long, no more than 32 characters")
    protected String name;

    @Schema(title = "unique code")
    protected String flowKey;

    @Size(max = 128, message = "description is too long, no more than 128 characters")
    @Schema(title = "description")
    protected String description;

    @Schema(title = "workflow template code")
    @NotNull(message = "template code must not be null")
    protected String templateCode;

    @Schema(title = "workflow category primary id")
    protected Integer categoryId;

    @Schema(title = "start trigger node code")
    private String triggerCode;


}
