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
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import io.innospots.workflow.node.app.compute.FuncType;
import io.innospots.workflow.node.app.compute.FunctionField;
import io.innospots.workflow.node.app.compute.ShiftFunctionField;
import io.innospots.workflow.node.app.utils.AppNodeUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;

import static io.innospots.workflow.node.app.compute.FunctionField.buildFuncFields;
import static io.innospots.workflow.node.app.compute.ShiftFunctionField.buildShiftFuncFields;

/**
 * like pandas group transform
 *
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class GroupTransformNode extends BaseNodeExecutor {

    private List<NodeParamField> dimensionFields;

    private NodeParamField listField;

    private List<FunctionField> rollingFields;

    private List<FunctionField> accumFields;

    private List<ShiftFunctionField> shiftFields;

    private String sourceFieldType;

    private FuncType funcType;

    private boolean outputRestricted;

//    public static final String FIELD_AGGREGATE = "aggregate_field";

    public static final String ROLLING_FIELDS = "rolling_fields";

    public static final String ACCUM_FIELDS = "accum_fields";

    public static final String SHIFT_FIELDS = "shift_fields";

    public static final String FIELD_DIMENSION_PAYLOAD = "dim_field_payload";

    public static final String FIELD_DIMENSION_LIST = "dim_field_list";

    public static final String FIELD_SOURCE_TYPE = "source_field_type";

    public static final String FIELD_PARENT_LIST = "list_parent_field";

    public static final String FUNC_TYPE = "func_type";

    public static final String FIELD_OUTPUT_RESTRICTED = "output_restricted";


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_SOURCE_TYPE);
//        validFieldConfig(FIELD_AGGREGATE);
        outputRestricted = valueBoolean(FIELD_OUTPUT_RESTRICTED);
        sourceFieldType = valueString(FIELD_SOURCE_TYPE);
        funcType = FuncType.valueOf(valueString(FUNC_TYPE));

        if ("payload".equals(sourceFieldType)) {
            dimensionFields = NodeInstanceUtils.buildParamFields(ni, FIELD_DIMENSION_PAYLOAD);
        } else if ("list".equals(sourceFieldType)) {
            dimensionFields = NodeInstanceUtils.buildParamFields(ni, FIELD_DIMENSION_LIST);
            listField = NodeInstanceUtils.buildParamField(ni, FIELD_PARENT_LIST);
        }

        if (funcType == FuncType.ACCUM) {
            accumFields = buildFuncFields(ni, ACCUM_FIELDS);
        } else if (funcType == FuncType.ROLLING) {
            rollingFields = buildFuncFields(ni, ROLLING_FIELDS);
        } else if (funcType == FuncType.COLUMN) {
            shiftFields = buildShiftFuncFields(ni);
        }
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        ArrayListValuedHashMap<String, Map<String, Object>> groupItems = AppNodeUtils.groupItems(nodeExecution, this.dimensionFields);
        List<Map<String, Object>> oItems = null;
        if (funcType == FuncType.ACCUM || funcType == FuncType.ROLLING) {
            oItems = computeAccumAndRolling(groupItems);
        } else if (funcType == FuncType.COLUMN) {
            oItems = computeShift(groupItems);
        }
        nodeOutput.setResults(oItems);
    }

    private List<Map<String, Object>> computeShift(ArrayListValuedHashMap<String, Map<String, Object>> groupItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, Collection<Map<String, Object>>> entry : groupItems.asMap().entrySet()) {
            List<Map<String, Object>> outList = new ArrayList<>(entry.getValue());
            outList = ShiftFunctionField.computeShift(outList, this.shiftFields, outputRestricted, log);
            Map<String, Object> dimItem = new LinkedHashMap<>();
            fillDimData(items, entry.getKey(), dimItem, outList);
        }
        return items;
    }

    private List<Map<String, Object>> computeAccumAndRolling(ArrayListValuedHashMap<String, Map<String, Object>> groupItems) {
        List<Map<String, Object>> items = new ArrayList<>();
        List<FunctionField> functionFields = this.funcType == FuncType.ACCUM ? this.accumFields : this.rollingFields;
        for (Map.Entry<String, Collection<Map<String, Object>>> entry : groupItems.asMap().entrySet()) {
            Map<String, Object> dimItem = new LinkedHashMap<>();
            //item.put(dimensionField.getCode(), entry.getKey());
            List<Map<String, Object>> outList = FunctionField.computeAccumAndRolling(entry.getValue(), funcType, functionFields, this.outputRestricted, log);
            fillDimData(items, entry.getKey(), dimItem, outList);
        }//end for

        return items;


    }

    private void fillDimData(List<Map<String, Object>> items, String key, Map<String, Object> dimItem, List<Map<String, Object>> outList) {
        String[] dims = key.split("~");
        for (int i = 0; i < dims.length; i++) {
            dimItem.put(dimensionFields.get(i).getCode(), dims[i]);
        }
        for (Map<String, Object> groupItem : outList) {
            dimItem.putAll(groupItem);
            items.add(dimItem);
        }
    }

}
