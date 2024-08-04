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

package io.innospots.workflow.core.node;


import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;


/**
 * @author Raydian
 * @date 2020/11/28
 */
@Setter
@Getter
public class NodeInfo {

    protected Integer nodeId;

    @NotNull(message = "node name cannot be empty")
    @Schema(title = "node display name")
    protected String name;

    @NotNull(message = "node code cannot be empty")
    @Schema(title = "node code")
    protected String code;

    @NotNull(message = "flow template code cannot be empty")
    @Schema(title = "flow template code")
    protected String flowCode;

    @NotNull(message = "node primitive type cannot be empty")
    @Schema(title = "node primitive type")
    protected NodePrimitive primitive;

    @NotNull(message = "node group cannot be empty")
    @Schema(title = "node group")
    protected Integer nodeGroupId;

    @Schema(title = "node icon image base64")
    protected String icon;

    @Schema(title = "node description")
    protected String description;

    @Schema(title = "has been used")
    protected Boolean used;

    @Schema(title = "the node can be delete")
    private Boolean deletable;

    @Schema(title = "node vendor")
    protected String vendor;

    @Schema(title = "node class name")
    protected String nodeType;

    @Schema(title = "node status, ONLINE or OFFLINE")
    protected DataStatus status;

    @Schema(title = "credential type code")
    protected String credentialTypeCode;

}
