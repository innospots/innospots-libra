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

import io.innospots.workflow.core.node.NodeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Getter
@Setter
@Schema(title = "NodeDefinition")
public class NodeDefinition extends NodeInfo {


//    @Schema(title = "node css style")
//    protected Map<String, Object> style;

//    @Schema(title = "the node running method when click the execution button")
//    private RunMethod runMethod;

//    @Schema(title = "input ports, only use in frontend which define the input ports")
//    private List<Map<String, Object>> inPorts;

//    @Schema(title = "output ports, only use in frontend which define the output ports")
//    private List<Map<String, Object>> outPorts;

    @Schema(title = "the node has delete icon in the canvas")
    private Boolean deletable;

    @Schema(title = "node config form element")
//    private Map<String, Object> config;
    private NodeResource resources;

    @Schema(title = "node setting")
    private Map<String,Object> settings;
//    private NodeSetting settings;

//    @Schema(title = "connector config info")
//    private List<Map<String, Object>> configs;

    @Schema(title = "execute script code")
    private Map<String, Object> scripts;

//    @Schema(title = "the setting of the execution pop panel, includes size and elements")
//    private Map<String, Object> executionPreview;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("nodeId=").append(nodeId);
        sb.append(", status=").append(status);
        sb.append(", name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", nodeType='").append(nodeType).append('\'');
        sb.append(", icon='").append(icon).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
