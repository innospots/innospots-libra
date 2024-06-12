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

package io.innospots.workflow.node.app.dataset;

import io.innospots.base.model.Pair;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import static io.innospots.workflow.node.app.dataset.FilterNode.splitSet;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class MergeNode extends BaseNodeExecutor {


    private MergeMode mergeMode;
    private String mainSourceNodeKey;

    public static final String FIELD_MERGE_MODE = "merge_mode";
    public static final String MAIN_SOURCE_NODE = "main_source_node";


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_MERGE_MODE);
        validSourceNodeSize(2);
        mainSourceNodeKey = valueString(MAIN_SOURCE_NODE);
        if (mainSourceNodeKey == null) {
            mainSourceNodeKey = ni.getPrevNodeKeys().get(0);
        }
        mergeMode = MergeMode.valueOf(valueString(FIELD_MERGE_MODE));
        log.debug("build node:{}, {}", this.nodeKey(), mergeMode);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = buildOutput(nodeExecution);
        validInputs(nodeExecution.getInputs(), 2);

        Pair<ExecutionInput, ExecutionInput> pair = splitSet(nodeExecution, mainSourceNodeKey);
        ExecutionInput oneInput = pair.getLeft();
        ExecutionInput secondInput = pair.getRight();

        for (ExecutionInput input : nodeExecution.getInputs()) {
            if (input.getSourceKey().equals(mainSourceNodeKey)) {
                oneInput = input;
            } else {
                secondInput = input;
            }
        }

        List<Map<String, Object>> mergeSet = null;
        assert oneInput != null;
        assert secondInput != null;

        switch (this.mergeMode) {
            case JOIN_DISTINCT:
                mergeSet = unionMerge(oneInput, secondInput);
                break;
            case INTERSECT:
                mergeSet = intersectMerge(oneInput, secondInput);
                break;
            case EXCEPT:
                mergeSet = exceptMerge(oneInput, secondInput);
                break;
            case JOIN_ALL:
                mergeSet = unionAllMerge(oneInput, secondInput);
                break;
            default:
        }

        nodeOutput.setResults(mergeSet);

    }

    private List<Map<String, Object>> unionMerge(ExecutionInput oneInput, ExecutionInput secondInput) {
        LinkedHashSet<Map<String, Object>> mergeSet = new LinkedHashSet<>(oneInput.getData());
        mergeSet.addAll(secondInput.getData());

        return new ArrayList<>(mergeSet);
    }

    private List<Map<String, Object>> unionAllMerge(ExecutionInput oneInput, ExecutionInput secondInput) {
        List<Map<String, Object>> mergeSet = new ArrayList<>(oneInput.getData());
        mergeSet.addAll(secondInput.getData());
        return mergeSet;
    }

    private List<Map<String, Object>> intersectMerge(ExecutionInput oneInput, ExecutionInput secondInput) {
        LinkedHashSet<Map<String, Object>> mergeSet = new LinkedHashSet<>(oneInput.getData());
        LinkedHashSet<Map<String, Object>> secondSet = new LinkedHashSet<>(secondInput.getData());
        mergeSet.retainAll(secondSet);

        return new ArrayList<>(mergeSet);
    }

    private List<Map<String, Object>> exceptMerge(ExecutionInput oneInput, ExecutionInput secondInput) {
        LinkedHashSet<Map<String, Object>> mergeSet = new LinkedHashSet<>(oneInput.getData());
        LinkedHashSet<Map<String, Object>> secondSet = new LinkedHashSet<>(secondInput.getData());
        mergeSet.removeAll(secondSet);

        return new ArrayList<>(mergeSet);
    }

}
