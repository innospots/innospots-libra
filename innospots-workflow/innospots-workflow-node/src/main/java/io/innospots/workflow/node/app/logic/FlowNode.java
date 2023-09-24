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

import io.innospots.base.condition.Factor;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.engine.FlowEngineManager;
import io.innospots.workflow.core.enums.OutputFieldMode;
import io.innospots.workflow.core.enums.OutputFieldType;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.field.InputField;
import io.innospots.workflow.core.node.instance.NodeInstance;
import io.innospots.workflow.core.runtime.FlowEventBody;
import io.innospots.workflow.node.app.StateNode;
import io.innospots.workflow.node.app.data.BaseDataNode;
import io.innospots.workflow.node.app.utils.NodeInstanceUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/20
 */
public class FlowNode extends BaseDataNode {


    public static final String SUB_FLOW_KEY = "sub_flow_key";
    public static final String INPUT_FIELDS = "input_fields";


    public static final String FLOW_TYPE = "flow_type";

    public static final String SYNC_SWITCH = "sync_switch";

    private String flowKey;

    private boolean syncSwitch;

    private List<InputField> inputFields;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        syncSwitch = nodeInstance.valueBoolean(SYNC_SWITCH);
        flowKey = this.valueString(SUB_FLOW_KEY);
        inputFields = NodeInstanceUtils.convertToList(nodeInstance,INPUT_FIELDS,InputField.class);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        List<Map<String, Object>> payloads = new ArrayList<>();
        NodeOutput nodeOutput = this.buildOutput(nodeExecution);
        for (ExecutionInput input : nodeExecution.getInputs()) {
            for (Map<String, Object> item : input.getData()) {
                Object res = null;
                FlowEventBody eventBody = new FlowEventBody();
                eventBody.setNodeCode(StateNode.StateNodeType.START.name());
                eventBody.setFlowKey(flowKey);
                Map<String,Object> nItem = new LinkedHashMap<>();
                for (InputField inputField : this.inputFields) {
                    if(inputField.getField()!=null){
                        nItem.put(inputField.getCode(),item.get(inputField.getField().getCode()));
                    }
                }
                eventBody.setBody(nItem);
                if (syncSwitch) {
                    res = EventBusCenter.getInstance().post(eventBody);
                } else {
                    EventBusCenter.getInstance().asyncPost(eventBody);
                }
                if (res == null) {
                    return;
                }
                if (res instanceof Map) {
                    nodeOutput.addResult((Map<String, Object>) res);
                } else if (res instanceof List) {
                    nodeOutput.setResults((List<Map<String, Object>>) res);
                } else {
                    nodeOutput.addResult(BeanUtils.toMap(res, false, false));
                }
            }
        }//end for

    }

}
