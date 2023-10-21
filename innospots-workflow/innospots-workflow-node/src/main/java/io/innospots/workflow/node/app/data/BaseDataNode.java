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

package io.innospots.workflow.node.app.data;

import io.innospots.base.condition.Factor;
import io.innospots.base.data.ap.IDataOperatorPoint;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.enums.OutputFieldMode;
import io.innospots.workflow.core.enums.OutputFieldType;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class BaseDataNode extends BaseAppNode {

    private static final Logger logger = LoggerFactory.getLogger(BaseDataNode.class);

    /**
     * store mode ,see OutputFieldMode
     */
    public static final String FIELD_OUTPUT_MODE_MAP = "output_mode_map";

    public static final String FIELD_OUTPUT_MODE_LIST = "output_mode_list";

    public static final String FIELD_OUTPUT_TYPE = "output_field_type";
    /**
     * store field variable name
     */
    public static final String FIELD_VARIABLE = "variable_name";


    protected OutputFieldMode outputFieldMode;

    protected OutputFieldType outputFieldType;

    protected String outputField;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
    }

    protected void fillOutputConfig(NodeInstance nodeInstance) {
        validFieldConfig(nodeInstance, FIELD_OUTPUT_TYPE);
        this.outputFieldType = OutputFieldType.valueOf(nodeInstance.valueString(FIELD_OUTPUT_TYPE));

        if(this.outputFieldType != OutputFieldType.NONE){
            if (!nodeInstance.containsKey(FIELD_OUTPUT_MODE_MAP) && !nodeInstance.containsKey(FIELD_OUTPUT_MODE_LIST)) {
                throw ConfigException.buildMissingException(this.getClass(), "nodeKey:" + nodeKey() + ", field:" + FIELD_OUTPUT_MODE_LIST+" or "+FIELD_OUTPUT_MODE_MAP);
            }
        }

        String outMode = nodeInstance.valueString(FIELD_OUTPUT_MODE_MAP);
        if (StringUtils.isNotEmpty(outMode)) {
            this.outputFieldMode = OutputFieldMode.valueOf(outMode);
        }

        if (this.outputFieldMode == null) {
            this.outputFieldMode = OutputFieldMode.valueOf(nodeInstance.valueString(FIELD_OUTPUT_MODE_LIST));
        }

        if (this.outputFieldMode == OutputFieldMode.FIELD) {
            validFieldConfig(nodeInstance, FIELD_VARIABLE);
            this.outputField = nodeInstance.valueString(FIELD_VARIABLE);
        }
    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }


    protected void fillOutput(NodeOutput nodeOutput, Map<String, Object> input) {
        fillOutput(nodeOutput, input, null);
    }

    protected void fillOutput(NodeOutput nodeOutput, Map<String, Object> input, Object body) {
        if(outputFieldType == OutputFieldType.NONE){
            return;
        }
        Map<String, Object> result = new LinkedHashMap<>();

        if (this.outputFieldMode != OutputFieldMode.OVERWRITE && input != null) {
            result.putAll(input);
            nodeOutput.addResult(result);
        }

        if (body == null) {
            return;
        }
        switch (this.outputFieldMode) {
            case FIELD:
                result.put(this.outputField, body);
                nodeOutput.addResult(result);
                break;
            case PAYLOAD:
                if (body instanceof Map) {
                    result.putAll((Map<String, Object>) body);
                } else if (body instanceof List) {
                    List l = (List) body;
                    result.putAll((Map<? extends String, ?>) l.get(0));
                    log.warn("fill value is collection type, select first item:{}", l.get(0));
                } else {
                    result.put(this.nodeKey(), JSONUtils.toJsonString(body));
                    log.error("the type of value is not correct,{}, {}", body, body.getClass());
                }
                nodeOutput.addResult(result);
                break;
            case OVERWRITE:
                if (body instanceof Map) {
                    result.putAll((Map<String, Object>) body);
                    nodeOutput.addResult(result);
                } else if (body instanceof List) {
                    ((List<?>) body).forEach(item -> {
                        if (item instanceof Map) {
                            nodeOutput.addResult((Map<String, Object>) item);
                        } else {
                            nodeOutput.addResult(JSONUtils.objectToMap(item));
                            log.error("the type of value is not correct,{}, {}", item, item.getClass());
                        }
                    });
                } else {
                    result.put(this.nodeKey(), JSONUtils.toJsonString(body));
                    nodeOutput.addResult(result);
                    log.error("the type of value is not correct,{}, {}", body, body.getClass());
                }
            default:
        }//end switch

    }


    protected List<Factor> conditionValues(Map<String, Object> input, List<Factor> factors) {
        List<Factor> conditions = new ArrayList<>();
        if (factors == null) {
            return conditions;
        }
        for (Factor conditionField : factors) {
            Factor fc = BeanUtils.copyProperties(conditionField, Factor.class);
            fc.setValue(conditionField.value(input));
            conditions.add(fc);
        }
        return conditions;
    }
}
