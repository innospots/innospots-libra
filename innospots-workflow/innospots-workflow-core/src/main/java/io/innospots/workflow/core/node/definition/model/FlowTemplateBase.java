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

package io.innospots.workflow.core.node.definition.model;

import io.innospots.base.enums.DataStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/28
 */
@Getter
@Setter
@Schema(title = "workflow template info")
public class FlowTemplateBase {

    @Schema(title = "workflow template primary id")
    private Integer flowTplId;

    @Schema(title = "template status")
    private DataStatus status;

    @NotNull
    @Max(value = 32)
    @Schema(title = "template name")
    protected String tplName;

    @NotNull
    @Max(value = 32)
    @Schema(title = "template code")
    protected String tplCode;

    @Pattern(regexp = "EVENT|SCHEDULE")
    @Schema(title = "template type, option values: EVENT, SCHEDULE")
    protected String type;

    @Max(value = 128)
    @Schema(title = "template description")
    protected String description;

}
