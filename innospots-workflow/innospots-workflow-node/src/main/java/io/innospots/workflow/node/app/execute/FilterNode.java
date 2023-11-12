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

package io.innospots.workflow.node.app.execute;

import io.innospots.base.exception.ConfigException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.Pair;
import io.innospots.workflow.core.exception.FlowRuntimeException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Smars
 * @date 2021/3/16
 */
public class FilterNode extends BaseNodeExecutor {


    private FilterMode filterMode;
    private String mainSourceNodeKey;

    public static final String FIELD_MERGE_MODE = "filter_mode";
    public static final String FIELD_FILTER_FIELDS = "filter_fields";
    public static final String MAIN_SOURCE_NODE = "main_source_node";

    /**
     * 过滤条件字段
     */
    private List<Pair<NodeParamField, NodeParamField>> filterFields;

    @Override
    public void initialize() {
        validFieldConfig(FIELD_FILTER_FIELDS);
        mainSourceNodeKey = valueString(MAIN_SOURCE_NODE);
        filterMode = FilterMode.valueOf(validString(FIELD_MERGE_MODE));
        filterFields = JoinNode.convertJoinFactor(ni, FIELD_FILTER_FIELDS);
        mainSourceNodeKey = filterFields.get(0).getLeft().getNodeKey();

        if (mainSourceNodeKey == null) {
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + this.nodeKey(), "field: " + mainSourceNodeKey);
        }
    }

    public static Pair<ExecutionInput, ExecutionInput> splitSet(NodeExecution nodeExecution, String mainSourceNodeKey) {
        if (nodeExecution.inputSize() <= 1) {
            throw FlowRuntimeException.buildException(FilterNode.class,"the size of node execution input must have more than 2","");
        }
        ExecutionInput mainInput = null;
        ExecutionInput filterInput = null;
        for (ExecutionInput input : nodeExecution.getInputs()) {
            if (mainSourceNodeKey == null) {
                if (mainInput == null) {
                    mainInput = input;
                } else {
                    filterInput = input;
                }
            } else if (input.getSourceKey().equals(mainSourceNodeKey)) {
                mainInput = input;
            } else {
                filterInput = input;
            }
        }
        return Pair.of(mainInput, filterInput);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        Pair<ExecutionInput, ExecutionInput> pair = splitSet(nodeExecution, mainSourceNodeKey);
        ExecutionInput mainInput = pair.getLeft();
        ExecutionInput filterInput = pair.getRight();
        NodeOutput nodeOutput = buildOutput(nodeExecution);

        if (mainInput == null) {
            throw ResourceException.buildAbandonException(this.getClass(),
                    "nodeKey: " + this.nodeKey(),
                    "main input is null", prevNodeKeys());
        }
        if (filterInput == null) {
            throw ResourceException.buildAbandonException(this.getClass(),
                    "nodeKey: " + this.nodeKey(),
                    "filter input is null", prevNodeKeys());
        }
        Set<String> filterSet = new HashSet<>();
        for (Map<String, Object> item : filterInput.getData()) {
            filterSet.add(itemKey(item));
        }//filter list

        for (Map<String, Object> item : mainInput.getData()) {
            String key = itemKey(item);
            boolean contain = filterSet.contains(key);
            if (this.filterMode == FilterMode.RESERVED && contain) {
                nodeOutput.addResult(item);
            }
            if (this.filterMode == FilterMode.REJECT && !contain) {
                nodeOutput.addResult(item);
            }
        }//end for mainInput


    }

    private String itemKey(Map<String, Object> item) {
        StringBuilder key = new StringBuilder();
        for (Pair<NodeParamField, NodeParamField> filterField : filterFields) {
            key.append(item.get(filterField.getRight().getCode()));
            key.append("_");
        }//end for filter key

        return key.toString();
    }


    /**
     * 过滤节点模式
     *
     * @author Smars
     * @date 2021/9/19
     */
    public enum FilterMode {

        /**
         *
         */
        RESERVED,
        REJECT;

    }

}
