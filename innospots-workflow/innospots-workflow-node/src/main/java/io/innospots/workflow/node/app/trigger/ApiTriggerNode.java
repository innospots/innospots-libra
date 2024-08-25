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

package io.innospots.workflow.node.app.trigger;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.core.runtime.webhook.FlowWebhookConfig;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * api trigger node is a webhook, which has not previous node
 * config webhook response field
 *
 * @author Smars
 * @date 2021/4/24
 */
public class ApiTriggerNode extends TriggerNode {

    public static final String FIELD_API_PATH = "path";

    public static final String FIELD_REQUEST_TYPE = "requestType";
    public static final String FIELD_RESPONSE_MODE = "responseMode";
    public static final String FIELD_REQUEST_FIELDS = "request_fields";


    private FlowWebhookConfig flowWebhookConfig;


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_API_PATH);
        validFieldConfig(FIELD_REQUEST_TYPE);
        validFieldConfig(FIELD_RESPONSE_MODE);
        flowWebhookConfig = new FlowWebhookConfig();
        flowWebhookConfig.setRequestMethod(FlowWebhookConfig.RequestMethod.valueOf(valueString(FIELD_REQUEST_TYPE)));
        flowWebhookConfig.setPath(valueString(FIELD_API_PATH));
        triggerInfo.put(FIELD_API_PATH,flowWebhookConfig.getPath());
        flowWebhookConfig.setResponseMode(FlowWebhookConfig.ResponseMode.valueOf(valueString(FIELD_RESPONSE_MODE)));

        List<Map<String, Object>> responseField = (List<Map<String, Object>>) value(FIELD_REQUEST_FIELDS);
        if (responseField != null) {
            List<ParamField> params = BeanUtils.toBean(responseField, ParamField.class);
            flowWebhookConfig.setRequestFields(params);
            ni.setInputFields(params);
        }
    }

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        super.invoke(nodeExecution, flowExecution);
    }

    @Override
    protected Object processItem(Map<String, Object> item) {
        if (item != null) {
            Object body = item.get("body");
            if (body != null) {
                return body;
            }
            body = item.get("params");
            if (body != null) {
                return body;
            }
        }

        if (item == null) {
            item = new LinkedHashMap<>();
            for (ParamField requestField : flowWebhookConfig.getRequestFields()) {
                item.put(requestField.getCode(), requestField.getValue());
            }
            return item;
        }
        return item;
    }

    public String apiPath() {
        return flowWebhookConfig != null ? flowWebhookConfig.getPath() : null;
    }


    public FlowWebhookConfig getFlowWebhookConfig() {
        return flowWebhookConfig;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("webhook path=").append(flowWebhookConfig.getPath());
        sb.append(" ,node key=").append(flowWebhookConfig.getPath());
        sb.append(" ,nodeType=").append(this.ni.getNodeType());
        sb.append('}');
        return sb.toString();
    }
}
