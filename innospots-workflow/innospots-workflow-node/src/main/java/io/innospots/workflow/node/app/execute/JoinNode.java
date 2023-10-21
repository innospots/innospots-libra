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

import io.innospots.base.data.enums.JoinType;
import io.innospots.base.model.Pair;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

import static io.innospots.workflow.node.app.execute.FilterNode.splitSet;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class JoinNode extends BaseAppNode {


    private JoinType joinType;
    private String mainSourceNodeKey;

    public static final String FIELD_JOIN_TYPE = "join_type";

    public static final String FIELD_JOIN_FIELDS = "join_fields";
    public static final String MAIN_SOURCE_NODE = "main_source_node";

    /**
     * 连接字段数据
     */
    private List<Pair<NodeParamField, NodeParamField>> joinFields;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(nodeInstance, FIELD_JOIN_TYPE);

        joinType = JoinType.valueOf(nodeInstance.valueString(FIELD_JOIN_TYPE));
        if(joinType!=JoinType.CROSS_JOIN){
            validFieldConfig(nodeInstance, FIELD_JOIN_FIELDS);
            joinFields = convertJoinFactor(nodeInstance, FIELD_JOIN_FIELDS);
            mainSourceNodeKey = joinFields.get(0).getLeft().getNodeKey();
        }


        log.debug("build node:{}, {}, {}", this.nodeKey(), joinType, joinFields);
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);

        Pair<ExecutionInput, ExecutionInput> pair = splitSet(nodeExecution, mainSourceNodeKey);
        ExecutionInput mainInput = pair.getLeft();
        ExecutionInput joinInput = pair.getRight();

        List<Map<String, Object>> joinList = null;
        switch (this.joinType) {
            case RIGHT_JOIN:
                joinList = joinLeftAndRight(joinInput, mainInput);
                break;
            case FULL_JOIN:
                joinList = outerJoin(mainInput, joinInput);
                break;
            case LEFT_JOIN:
                joinList = joinLeftAndRight(mainInput, joinInput);
                break;
            case INNER_JOIN:
                joinList = innerJoin(mainInput, joinInput);
                break;
            case CROSS_JOIN:
                joinList = crossJoin(mainInput, joinInput);
            default:
                break;
        }
        nodeOutput.setResults(joinList);
    }

    private List<Map<String, Object>> crossJoin(ExecutionInput mainInput, ExecutionInput joinInput) {
        List<Map<String, Object>> ll = new ArrayList<>();
        for (Map<String, Object> mainData : mainInput.getData()) {
            for (Map<String, Object> secondData : joinInput.getData()) {
                LinkedHashMap<String, Object> data = new LinkedHashMap<>(mainData);
                data.putAll(secondData);
                ll.add(data);
            }
        }

        return ll;
    }

    private List<Map<String, Object>> joinLeftAndRight(ExecutionInput mainInput, ExecutionInput joinInput) {
        List<Map<String, Object>> ll = new ArrayList<>();
        Map<String, Map<String, Object>> joinItems = joinMap(joinInput);
        for (Map<String, Object> item : mainInput.getData()) {
            String key = itemKey(item);
            Map<String, Object> joinItem = joinItems.get(key);
            LinkedHashMap<String, Object> newItem = new LinkedHashMap<>(item);
            if (joinItem != null) {
                newItem.putAll(joinItem);
            }
            ll.add(newItem);
        }
        return ll;
    }

    private List<Map<String, Object>> innerJoin(ExecutionInput mainInput, ExecutionInput joinInput) {
        List<Map<String, Object>> ll = new ArrayList<>();
        Map<String, Map<String, Object>> joinItems = joinMap(joinInput);
        for (Map<String, Object> item : mainInput.getData()) {
            String key = itemKey(item);
            Map<String, Object> joinItem = joinItems.get(key);
            if (joinItem != null) {
                LinkedHashMap<String, Object> newItem = new LinkedHashMap<>(item);
                newItem.putAll(joinItem);
                ll.add(newItem);
            }
        }
        return ll;
    }

    private List<Map<String, Object>> outerJoin(ExecutionInput mainInput, ExecutionInput joinInput) {
        List<Map<String, Object>> ll = new ArrayList<>();
        Map<String, Map<String, Object>> joinItems = joinMap(joinInput);
        for (Map<String, Object> item : mainInput.getData()) {
            String key = itemKey(item);
            Map<String, Object> joinItem = joinItems.get(key);
            LinkedHashMap<String, Object> newItem = new LinkedHashMap<>(item);
            if (joinItem != null) {
                newItem.putAll(joinItem);
                ll.add(newItem);
                joinItems.remove(key);
            }
        }//end main item

        if (!joinItems.isEmpty()) {
            ll.addAll(joinItems.values());
        }//end if

        return ll;
    }

    private Map<String, Map<String, Object>> joinMap(ExecutionInput joinInput) {
        Map<String, Map<String, Object>> joinItems = new HashMap<>();
        for (Map<String, Object> item : joinInput.getData()) {
            joinItems.put(itemKey(item), item);
        }
        return joinItems;
    }

    private String itemKey(Map<String, Object> item) {
        StringBuilder key = new StringBuilder();
        for (Pair<NodeParamField, NodeParamField> filterField : joinFields) {
            key.append(item.get(filterField.getRight().getCode()));
            key.append("_");
        }//end for filter key

        return key.toString();
    }


    public static List<Pair<NodeParamField, NodeParamField>> convertJoinFactor(NodeInstance nodeInstance, String fieldName) {
        List<Map<String, Object>> joiners = (List<Map<String, Object>>) nodeInstance.value(fieldName);
        List<Pair<NodeParamField, NodeParamField>> joinFields = new ArrayList<>();
        for (Map<String, Object> filterMap : joiners) {
            Pair<Map, Map> pair = BeanUtils.toBean(filterMap, Pair.class);
            joinFields.add(Pair.of(BeanUtils.toBean(pair.getLeft(), NodeParamField.class),
                    BeanUtils.toBean(pair.getRight(), NodeParamField.class)
            ));
        }
        return joinFields;
    }

}
