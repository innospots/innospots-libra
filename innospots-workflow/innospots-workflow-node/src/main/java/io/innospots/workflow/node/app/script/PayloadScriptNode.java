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

package io.innospots.workflow.node.app.script;

import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * script execute node
 *
 * @author Smars
 * @date 2021/4/20
 */
@Slf4j
public class PayloadScriptNode extends ScriptBaseNode {


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        if (scriptExecutor != null) {
            nodeOutput.addNextKey(this.nextNodeKeys());
            List<Map<String, Object>> items = new ArrayList<>();
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                //end for
                items.addAll(executionInput.getData());
            }//end execution input
            Object result = scriptExecutor.execute(items);
            processOutput(nodeExecution,result, nodeOutput);
        }//end if
        nodeExecution.addOutput(nodeOutput);
        log.debug("node execution, nodeOutput:{} {}", nodeOutput, nodeExecution);
    }

}
