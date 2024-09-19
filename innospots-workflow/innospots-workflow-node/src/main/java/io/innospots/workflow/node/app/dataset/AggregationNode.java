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

import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import io.innospots.workflow.node.app.compute.AggregationComputeField;
import io.innospots.workflow.node.app.utils.AppNodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

        computeFields = AppNodeUtils.buildAggregationComputeFields(ni,FIELD_AGGREGATE);

    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        List<Map<String, Object>> items = new ArrayList<>();
        ArrayListValuedHashMap<String, Map<String, Object>> groupItems = AppNodeUtils.groupItems(nodeExecution,dimensionFields);

        items = AppNodeUtils.computeAggregateFields(groupItems,computeFields,dimensionFields);
        nodeOutput.setResults(items);
    }

}
