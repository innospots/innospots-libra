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

package io.innospots.workflow.node.app.compute;

import io.innospots.base.model.field.ComputeField;
import io.innospots.base.model.field.compute.ComputeItem;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/8/22
 */
public class DerivedVariableNode extends BaseNodeExecutor {

    private static final Logger logger = LoggerFactory.getLogger(DerivedVariableNode.class);

    private List<ComputeField> computeFields;

    /**
     * reset
     */
    private boolean outputRestricted;

    public static final String FIELD_COMPUTE = "compute_fields";

    /**
     * boolean value, only the variables in the variable list are output
     */
    public static final String FIELD_OUTPUT_RESTRICTED = "output_restricted";


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_COMPUTE);
        computeFields = buildFields(ni);
        if (CollectionUtils.isNotEmpty(computeFields)) {
            for (ComputeField computeField : computeFields) {
                computeField.initialize();
            }//end for
        }
        outputRestricted = valueBoolean(FIELD_OUTPUT_RESTRICTED);
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        List<Map<String, Object>> outData = new ArrayList<>();
        StringBuilder error = new StringBuilder();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> out = new LinkedHashMap<>();
                for (ComputeField computeField : this.computeFields) {
                    try {
                        Object result = computeField.compute(item);
                        out.put(computeField.getCode(), result);
                    } catch (Exception e) {
                        logger.error("compute field failed:{}, data:{}", computeField, item, e);
                        error.append(computeField.getCode());
                        error.append(", ");
                        error.append(computeField.getExpr());
                        error.append(" ,error: ");
                        error.append(e.getMessage());
                    }
                }
                if (!this.outputRestricted) {
                    out.putAll(item);
                }
                outData.add(out);
            }//end for item
        }//end for execution input
        if (error.length() > 0) {
            nodeExecution.setMessage(error.toString());
            nodeExecution.setStatus(ExecutionStatus.FAILED);
        }
        nodeOutput.setResults(outData);
    }


    /*
    @Override
    public List<MethodBody> expMethods(NodeInstance nodeInstance) {
        List<MethodBody> methodBodies = new ArrayList<>();
        List<ComputeField> cfs = buildFields(nodeInstance);

        for (ComputeField computeField : cfs) {
            MethodBody methodBody = new MethodBody(ScriptType.JAVA);
            String exp = null;
            try {
                Map<String,Object> dataModel = new HashMap<>();
                exp = computeField.toExpScript(FunctionScript.SCRIPT);
                dataModel.put("expression",exp);
                dataModel.put("returnClass",computeField.getValueType().getClazz().getName());
                String src = SourceTemplateUtils.output(this.getClass().getSimpleName()+".ftl",dataModel);
                methodBody.setSrcBody(src);
                methodBody.setMethodName(this.scriptName(computeField.getCode()));
                methodBody.setReturnType(computeField.getValueType().getClazz());
                methodBodies.add(methodBody);
            } catch (IOException | TemplateException e) {
                logger.error(e.getMessage(),e);
                throw ConfigException.buildTypeException(this.getClass(),"if template invalid format, nodeKey:"+nodeKey + ", error: "+e.getMessage());
            }
            if(logger.isDebugEnabled()){
                logger.debug("node nodeKey:{}, source: {}",this.nodeKey,exp);
            }
        }//end for

        return methodBodies;
    }
 */

    public static List<ComputeField> buildFields(NodeInstance nodeInstance) {
        List<Map<String, Object>> fieldMaps = (List<Map<String, Object>>) nodeInstance.value(FIELD_COMPUTE);

        List<ComputeField> computeFields = new ArrayList<>();
        if (CollectionUtils.isEmpty(fieldMaps)) {
            return computeFields;
        }
        for (Map<String, Object> fieldMap : fieldMaps) {
            ComputeField cf = BeanUtils.toBean(fieldMap, ComputeField.class);
            List<Map<String, Object>> items = (List<Map<String, Object>>) fieldMap.get("computeItems");
            cf.setComputeItems(convertItems(items));
            computeFields.add(cf);
        }
        return computeFields;
    }

    private static List<ComputeItem> convertItems(List<Map<String, Object>> items) {
        List<ComputeItem> computeItems = new ArrayList<>();
        for (Map<String, Object> item : items) {
            computeItems.add(convert(item));
        }
        return computeItems;
    }

    private static ComputeItem convert(Map<String, Object> item) {
        Object data = item.get("data");
        List<List<ComputeItem>> computeItems = null;
        if (data instanceof List && !((List) data).isEmpty()) {
            computeItems = convertItemData((List<List<Map<String, Object>>>) data);
        }
        ComputeItem computeItem = BeanUtils.toBean(item, ComputeItem.class);
        if (computeItems != null) {
            computeItem.setData(computeItems);
        }
        return computeItem;
    }

    private static List<List<ComputeItem>> convertItemData(List<List<Map<String, Object>>> data) {
        List<List<ComputeItem>> items = new ArrayList<>();
        for (List<Map<String, Object>> item : data) {
            items.add(convertItems(item));
        }
        return items;
    }
}
