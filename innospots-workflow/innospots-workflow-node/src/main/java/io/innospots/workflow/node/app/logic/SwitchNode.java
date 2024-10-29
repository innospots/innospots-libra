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

package io.innospots.workflow.node.app.logic;


import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.condition.BaseCondition;
import io.innospots.base.condition.Relation;
import io.innospots.base.exception.ConfigException;
import io.innospots.script.base.IScriptExecutor;
import io.innospots.script.base.aviator.AviatorExpressionExecutor;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * scripts contain json array object, the class of which is SwitchCondition
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Slf4j
public class SwitchNode extends BaseNodeExecutor {

    public static final String FIELD_CONDITIONS = "conditions";
    public static final String FIELD_CONDITION_FIELD = "conditionField";

    private List<SwitchCondition> switchConditions;

    private List<String> defaultNextNodeKeys = new ArrayList<>();

    @Override
    protected void initialize() {
        switchConditions = buildConditions(ni);
        List<String> nextNodes = new ArrayList<>(this.nextNodeKeys());
        for (SwitchCondition switchCondition : switchConditions) {
            if (switchCondition.getCondition() == null) {
                defaultNextNodeKeys.addAll(nextNodes);
            }
            if (!nextNodes.isEmpty() && CollectionUtils.isNotEmpty(switchCondition.getNodeKeys())) {
                for (String nodeKey : switchCondition.getNodeKeys()) {
                    nextNodes.remove(nodeKey);
                }
            }

        }//end for switch condition
        defaultNextNodeKeys.addAll(nextNodes);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        Map<String, ExecutionOutput> outCache = new LinkedHashMap<>();
        ExecutionOutput defaultOutput = new ExecutionOutput("#default");
        if (switchConditions != null) {
            for (int i = 0; i < switchConditions.size(); i++) {
                SwitchCondition sc = switchConditions.get(i);
                String nName = sc.name(i + 1);
                ExecutionOutput nodeOutput = new ExecutionOutput(nName);
                nodeOutput.addNextKey(sc.getNodeKeys());
                outCache.put(sc.sourceAnchor, nodeOutput);
            }
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                for (Map<String, Object> data : executionInput.getData()) {
                    boolean isMatch = false;
                    for (SwitchCondition switchCondition : switchConditions) {
                        if (switchCondition.match(data)) {
                            isMatch = true;
                            ExecutionOutput nodeOutput = outCache.get(switchCondition.sourceAnchor);
                            nodeOutput.addResult(data);
                            break;
                        }//end if switch condition
                    }//end for switch conditions

                    if (!isMatch) {
                        defaultOutput.addResult(data);
                    }
                }//end for data
            }//end execution input
            defaultOutput.addNextKey(this.defaultNextNodeKeys);

            outCache.values().forEach(nodeExecution::addOutput);
            nodeExecution.addOutput(defaultOutput);
        }//end if
        if (log.isDebugEnabled()) {
            log.debug("node execution, nodeOutput:{} {}", outCache, nodeExecution);
        }
    }


    private List<SwitchCondition> buildConditions(NodeInstance nodeInstance) {
        Object v = nodeInstance.getData().get(FIELD_CONDITIONS);
        if (v == null) {
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", field:" + FIELD_CONDITIONS);
        }
        List<SwitchCondition> switchConditions = new ArrayList<>();
        try {
            List<Map<String,Object>> conditionsMap = (List<Map<String, Object>>) v;
            for (Map<String, Object> map : conditionsMap) {
                String sourceAnchor = (String) map.get("sourceAnchor");
                Map<String,Object> branch = (Map<String, Object>) map.get("branch");
                if(branch == null){
                    continue;
                }
                BaseCondition condition = BeanUtils.toBean(branch,BaseCondition.class);
                SwitchCondition sw = new SwitchCondition();
                sw.condition = condition;
                sw.condition.setRelation(Relation.AND);
                sw.sourceAnchor = sourceAnchor;
                switchConditions.add(sw);
            }
            //switchConditions = BeanUtils.toBean(conditionsMap,SwitchCondition.class);
//            switchConditions = JSONUtils.parseObject(JSONUtils.toJsonString(v), SwitchCondition[].class);
            for (SwitchCondition switchCondition : switchConditions) {
                switchCondition.initialize();
                if (this.nodeAnchors() != null) {
                    switchCondition.setNodeKeys(this.nodeAnchors().get(switchCondition.sourceAnchor));
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
        return switchConditions;
    }

    @Getter
    @Setter
    public static class SwitchCondition implements Initializer {
        @JsonIgnore
        private List<String> nodeKeys;
        private String sourceAnchor;
        private BaseCondition condition;
        @JsonIgnore
        private String statement;
        private String desc;

        @JsonIgnore
        private IScriptExecutor expression;

        public String name(int pos) {
            return desc != null ? desc : "#" + pos;
        }

        @Override
        public void initialize() {
            if (condition != null) {
                condition.initialize();
                this.statement = condition.getStatement();
                if (condition != null) {
                    expression = new AviatorExpressionExecutor(this.statement);
                }
            }
        }

        public boolean match(Map<String, Object> data) {
            if (expression == null) {
                return false;
            }
            return (Boolean) expression.execute(data);
        }
    }

}
