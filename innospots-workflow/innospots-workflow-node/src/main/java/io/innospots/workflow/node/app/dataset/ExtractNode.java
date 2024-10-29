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

import cn.hutool.core.builder.CompareToBuilder;
import cn.hutool.core.comparator.ComparatorChain;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.model.field.ParamField;
import io.innospots.script.base.IScriptExecutor;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Smars
 * @date 2021/3/16
 */
public class ExtractNode extends BaseNodeExecutor {


    private ExtractMode extractMode;

    public static final String FIELD_EXTRACT_MODE = "extract_mode";
    public static final String FIELD_EXTRACT_FIELDS = "extract_fields";
    public static final String FIELD_EMBED_FIELDS = "embed_fields";
    public static final String FIELD_ORDER = "order_fields";
    public static final String ORDER_TYPE = "order_type";
    public static final String LINE_COUNT = "line_count";
    public static final String START_LINE = "start_line";
    public static final String END_LINE = "end_line";
    public static final String EXTRACT_CONDITION = "extract_condition";

    private List<ParamField> extractFields;

    private String conditionExpression;

    private List<NodeParamField> orderFields;

    private OrderType orderType;

    private Integer lineCount;

    private Integer startLine;

    private Integer endLine;

    private IScriptExecutor expression;

    @Override
    protected void initialize() {
        extractMode = ExtractMode.valueOf(validString(FIELD_EXTRACT_MODE));
        if (extractMode == ExtractMode.FIELD) {
            validFieldConfig(FIELD_EXTRACT_FIELDS);
            List<Map<String, Object>> v = valueMapList(FIELD_EXTRACT_FIELDS);
            extractFields = BeanUtils.toBean(v, ParamField.class);
        } else if (extractMode == ExtractMode.EMBED) {
            validFieldConfig(FIELD_EMBED_FIELDS);
            List<Map<String, Object>> v = valueMapList(FIELD_EMBED_FIELDS);
            extractFields = BeanUtils.toBean(v, ParamField.class);
        }
        this.expression = NodeInstanceUtils.buildExpression(ni,EXTRACT_CONDITION,this);
        orderFields = NodeInstanceUtils.buildParamFields(ni,FIELD_ORDER);
        String oType = valueString(ORDER_TYPE);
        if(orderFields!=null){
            if(oType != null){
                orderType = OrderType.valueOf(oType);
            }else{
                orderType = OrderType.ASC;
            }

        }
        lineCount = valueInteger(LINE_COUNT);
        if(lineCount==null || lineCount ==0){
            lineCount = Integer.MAX_VALUE;
        }
        if(extractMode == ExtractMode.RANGE){
            startLine = valueInteger(START_LINE);
            endLine = valueInteger(END_LINE);
            if(startLine!=null && endLine!=null && startLine > endLine){
                throw ConfigException.buildParamException(this.getClass(),"startLine greater than endLine,",startLine,endLine);
            }
            if(startLine==null){
                startLine = 1;
            }
            if(endLine==null){
                endLine = Integer.MAX_VALUE;
            }
        }

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = buildOutput(nodeExecution);
        Set<Map<String, Object>> uniSet = new LinkedHashSet<>();
        List<Map<String,Object>> items = new ArrayList<>();
        int count = 0;
        AtomicInteger cntCounter = new AtomicInteger();
        int total = nodeExecution.getInputs().stream().mapToInt(f->f.getData().size()).sum();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                if(items.size() >= lineCount){
                    break;
                }
                count++;
                Map<String, Object> oItem = item;
                switch (extractMode) {
                    case REMOVE_EMPTY:
                        if (MapUtils.isEmpty(oItem)) {
                            continue;
                        } else {
                            addItem(items,oItem);
                        }
                        break;
                    case REMOVE_DUPLICATE:
                        uniSet.add(oItem);
                        break;
                    case HEAD:
                        if(count <= lineCount){
                            addItem(items,oItem);
                        }
                        break;
                    case TAIL:
                        if(count > (total - lineCount)){
                            addItem(items, oItem);
                        }
                        break;
                    case RANGE:
                        if(count>=startLine && count<=endLine){
                            addItem(items, oItem);
                        }
                        break;
                    case FIELD:
                    case EMBED:
                    default:
                        if (extractFields == null) {
                            continue;
                        }
                        if (extractFields.size() > 1) {
                            oItem = new LinkedHashMap<>(extractFields.size());
                            for (ParamField extractField : extractFields) {
                                Object v = item.get(extractField.getCode());
                                oItem.put(extractField.getCode(), v);
                            }
                            addItem(items,oItem);
                        } else {
                            Object eItem = extractValue(extractFields.get(0), oItem);
                            if (eItem instanceof Map) {
                                addItem(items, (Map<String, Object>)eItem);
                            } else if (eItem instanceof Collection) {
                                ((Collection<? extends Map<String, Object>>) eItem).forEach(m->{
                                    addItem(items,m);
                                });
                            }
                        }
                        break;
                }//end switch
            }//end for

        }//end for input
        if(!uniSet.isEmpty()){
            items.addAll(uniSet);
        }
        sort(items);
        nodeOutput.setResults(items);
    }

    private void sort(List<Map<String,Object>> items){
        if(orderFields==null || CollectionUtils.isEmpty(orderFields)){
            return;
        }
        ComparatorChain<Map<String, Object>> comparatorChain = new ComparatorChain<>();
        for (NodeParamField orderField : orderFields) {
            Comparator<Map<String, Object>> comparator = (o1, o2) -> CompareToBuilder.reflectionCompare(o1.get(orderField.getCode()),o2.get(orderField.getCode()));
            comparatorChain.addComparator(comparator,orderType == OrderType.DESC);
        }
        items.sort(comparatorChain);
    }

    private void addItem(List<Map<String,Object>> items,Map<String,Object> item){
        if(this.expression==null || this.expression.executeBoolean(item)){
            if(items.size() < lineCount){
                items.add(item);
            }
        }
    }

    private Object extractValue(ParamField extractField, Map<String, Object> item) {
        Object rValue = null;
        Map<String, Object> eItem = new LinkedHashMap<>(extractFields.size());
        Object value = item.get(extractField.getCode());
        if (value instanceof Map) {
            eItem.putAll((Map<? extends String, ?>) value);
        } else if (value instanceof Collection) {
            List<Map<String, Object>> list = new ArrayList<>();
            boolean mapData = true;
            for (Object v : (Collection) value) {
                if (v instanceof Map) {
                    list.add((Map<String, Object>) v);
                } else {
                    mapData = false;
                }
            }//end for
            if (!mapData) {
                eItem.put(extractField.getCode(), value);
            } else {
                rValue = list;
            }
        } else {
            eItem.put(extractField.getCode(), item.get(extractField.getCode()));
        }
        if (rValue == null) {
            rValue = eItem;
        }
        return rValue;
    }


    public enum ExtractMode {
        /**
         *
         */
        FIELD,
        EMBED,
        REMOVE_DUPLICATE,
        REMOVE_EMPTY,
        HEAD,
        TAIL,
        RANGE
        ;
    }

    public enum OrderType{
        ASC,
        DESC
    }
}
