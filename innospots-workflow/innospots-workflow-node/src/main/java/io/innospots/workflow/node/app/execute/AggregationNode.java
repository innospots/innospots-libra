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

import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * group by the dimension field and aggregate the summary field values
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class AggregationNode extends BaseNodeExecutor {


    private List<NodeParamField> dimensionFields;
    private NodeParamField listField;

    private List<AggregationComputeField> computeFields;

    public static final String FIELD_AGGREGATE = "aggregate_field";
    public static final String FIELD_DIMENSION_PAYLOAD = "dim_field_payload";
    public static final String FIELD_DIMENSION_LIST = "dim_field_list";
    public static final String FIELD_SOURCE_TYPE = "source_field_type";
    public static final String FIELD_PARENT_LIST = "list_parent_field";

    private String sourceFieldType;


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_AGGREGATE);
        sourceFieldType = validString(FIELD_SOURCE_TYPE);
        if("payload".equals(sourceFieldType)){
            dimensionFields = NodeInstanceUtils.buildParamFields(ni,FIELD_DIMENSION_PAYLOAD);
        }else if("list".equals(sourceFieldType)){
            dimensionFields = NodeInstanceUtils.buildParamFields(ni,FIELD_DIMENSION_LIST);
            listField = NodeInstanceUtils.buildParamField(ni,FIELD_PARENT_LIST);
        }

        computeFields = NodeInstanceUtils.buildAggregationComputeFields(ni,FIELD_AGGREGATE);

    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = this.buildOutput(nodeExecution);
        List<Map<String, Object>> items = new ArrayList<>();
        ArrayListValuedHashMap<String, Map<String, Object>> groupItems = new ArrayListValuedHashMap<>();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                String key = dimensionFields.stream().map(f-> String.valueOf(item.get(f.getCode()))).collect(Collectors.joining("~"));
                groupItems.put(key, item);
            }//end for item
        }//end for execution input
        for (Map.Entry<String, Collection<Map<String, Object>>> entry : groupItems.asMap().entrySet()) {
            Map<String, Object> item = new HashMap<>();
            //item.put(dimensionField.getCode(), entry.getKey());
            for (AggregationComputeField computeField : computeFields) {
                item.put(computeField.getCode(), computeField.compute(entry.getValue()));
            }
            String[] dims = entry.getKey().split("~");
            for (int i = 0; i < dims.length; i++) {
                item.put(dimensionFields.get(i).getCode(),dims[i]);
            }
            items.add(item);
        }
        nodeOutput.setResults(items);
    }

}
