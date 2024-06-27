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

package io.innospots.workflow.core.engine;


import io.innospots.workflow.core.exception.FlowPrepareException;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;

import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public interface IFlowEngine {


    BuildProcessInfo prepare(Long flowInstanceId, Integer version, boolean force) throws FlowPrepareException;

    /**
     * @param flowExecution
     */
    void execute(FlowExecution flowExecution);

    boolean continueExecute(FlowExecution flowExecution);

    FlowExecution stop(String flowExecutionId);

    FlowExecution stopByFlowKey(String flowKey);

    default FlowExecution run(Long workflowInstanceId,
                      Integer revisionId,
                      String targetNodeKey,
                      List<Map<String, Object>> payloads){
        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(
                workflowInstanceId,revisionId,payloads);
        flowExecution.resetCurrentNodeKey(targetNodeKey);

        execute(flowExecution);

        return flowExecution;
    }

}
