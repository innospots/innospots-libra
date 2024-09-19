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

import cn.hutool.core.comparator.ComparatorChain;
import cn.hutool.core.comparator.CompareUtil;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author Smars
 * @date 2021/3/16
 */
public class FiltrateOrderNode extends BaseNodeExecutor {

    public static final String FIELD_ASC = "asc_fields";
    public static final String FIELD_DESC = "desc_fields";
    public static final String LINE_COUNT = "line_count";
    public static final String FILTER_CONDITION = "filter_condition";

    private List<NodeParamField> descFields;

    private List<NodeParamField> ascFields;

    private Integer lineCount;

    private IScriptExecutor expression;

    @Override
    protected void initialize() {

        this.expression = NodeInstanceUtils.buildExpression(ni,FILTER_CONDITION,this);
        lineCount = valueInteger(LINE_COUNT);
        if(lineCount==null || lineCount ==0){
            lineCount = Integer.MAX_VALUE;
        }
        descFields = NodeInstanceUtils.buildParamFields(ni,FIELD_DESC);
        ascFields = NodeInstanceUtils.buildParamFields(ni,FIELD_ASC);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = buildOutput(nodeExecution);
        List<Map<String,Object>> items = new ArrayList<>();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                if(items.size() >= lineCount){
                    break;
                }
                addItem(items,item);
            }//end for

        }//end for input
        sort(items);
        nodeOutput.setResults(items);
    }

    private void sort(List<Map<String,Object>> items){
        if(CollectionUtils.isEmpty(ascFields) && CollectionUtils.isEmpty(descFields)){
            return;
        }
        ComparatorChain<Map<String,Object>> comparatorChain = new ComparatorChain<>();
        if(CollectionUtils.isNotEmpty(ascFields)){
            fillComparator(comparatorChain,ascFields,false);
        }
        if(CollectionUtils.isNotEmpty(descFields)){
            fillComparator(comparatorChain,descFields,true);
        }
        if(comparatorChain.size()>0){
            items.sort(comparatorChain);
        }
    }

    private void fillComparator(ComparatorChain<Map<String,Object>> comparatorChain,List<NodeParamField> paramFields,boolean reverse){
        for (NodeParamField orderField : paramFields) {
            Comparator<Map<String, Object>> comparator = (o1, o2) -> {
                Comparable c1 = (Comparable) o1.get(orderField.getCode());
                Comparable c2 = (Comparable) o2.get(orderField.getCode());
                return CompareUtil.compare(c1,c2,false);
            };
            comparatorChain.addComparator(comparator,reverse);
        }
    }

    private void addItem(List<Map<String,Object>> items,Map<String,Object> item){
        if(this.expression==null || this.expression.executeBoolean(item)){
            if(items.size() < lineCount){
                items.add(item);
            }
        }
    }

}
