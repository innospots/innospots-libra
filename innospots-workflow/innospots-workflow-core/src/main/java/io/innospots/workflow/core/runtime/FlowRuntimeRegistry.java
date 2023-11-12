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

package io.innospots.workflow.core.runtime;


import io.innospots.workflow.core.enums.FlowStatus;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/2
 */
@Getter
@Setter
public class FlowRuntimeRegistry {

    private BaseNodeExecutor registryNode;

    private Long workflowInstanceId;

    private Integer revision;

    private FlowStatus flowStatus;

    private String buildInfo;

    private String flowKey;

    private LocalDateTime updateTime;


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("").append(registryNode);
        sb.append('}');
        return sb.toString();
    }

    public String key() {
        return "f_" + workflowInstanceId + "_" + revision + "_" + registryNode.nodeKey();
    }

    public Map<String, Object> info() {
        Map<String, Object> info = new HashMap<>();
        info.put("key", key());
        info.put("registry", registryNode);
        return info;
    }
}
