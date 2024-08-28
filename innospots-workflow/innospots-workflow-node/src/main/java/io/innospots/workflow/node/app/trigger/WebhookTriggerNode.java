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

import cn.hutool.core.codec.Base64;
import cn.hutool.http.Header;
import cn.hutool.http.HttpStatus;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.core.runtime.webhook.FlowWebhookConfig;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.List;
import java.util.Map;

/**
 * api trigger node is a webhook, which has not previous node
 * config webhook response field
 *
 * @author Smars
 * @date 2021/4/24
 */
public class WebhookTriggerNode extends TriggerNode {

    public static final String FIELD_API_PATH = "path";

    public static final String FIELD_REQUEST_TYPE = "requestType";
    public static final String FIELD_AUTH_TYPE = "authType";
    public static final String FIELD_AUTH_BODY = "authBody";
    public static final String FIELD_RESPONSE_MODE = "responseMode";
    public static final String FIELD_RESPONSE_CODE = "responseCode";
    public static final String FIELD_RESPONSE_DATA = "responseData";
    public static final String FIELD_RESPONSE_FIELDS = "responseFields";
    public static final String FIELD_REQUEST_INPUTS = "requestInputs";

    private FlowWebhookConfig flowWebhookConfig;


    @Override
    protected void initialize() {
        validFieldConfig(FIELD_API_PATH);
        validFieldConfig(FIELD_AUTH_TYPE);
        validFieldConfig(FIELD_RESPONSE_CODE);
        validFieldConfig(FIELD_REQUEST_TYPE);
        validFieldConfig(FIELD_RESPONSE_MODE);
        validFieldConfig(FIELD_RESPONSE_DATA);
        flowWebhookConfig = new FlowWebhookConfig();
        flowWebhookConfig.setRequestMethod(FlowWebhookConfig.RequestMethod.valueOf(valueString(FIELD_REQUEST_TYPE)));
        flowWebhookConfig.setPath(valueString(FIELD_API_PATH));
        flowWebhookConfig.setAuthType(FlowWebhookConfig.AuthType.valueOf(valueString(FIELD_AUTH_TYPE)));
        flowWebhookConfig.setResponseMode(FlowWebhookConfig.ResponseMode.valueOf(valueString(FIELD_RESPONSE_MODE)));
        flowWebhookConfig.setResponseCode(ni.valueString(FIELD_RESPONSE_CODE));

        List<Map<String, Object>> responseField = (List<Map<String, Object>>) value("responseFields");
        if (responseField != null) {
            List<ParamField> params = BeanUtils.toBean(responseField, ParamField.class);
            flowWebhookConfig.setResponseFields(params);
        }

        if(flowWebhookConfig.getAuthType() == FlowWebhookConfig.AuthType.BASIC_AUTH){

            flowWebhookConfig.auth(this.valueString(FlowWebhookConfig.BASIC_AUTH_USERNAME),this.valueString(FlowWebhookConfig.BASIC_AUTH_PASSWORD));
        }

        if(flowWebhookConfig.getAuthType() == FlowWebhookConfig.AuthType.BEARER_AUTH){
            flowWebhookConfig.auth(this.valueString(FlowWebhookConfig.BEARER_AUTH_TOKEN));
        }
    }

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        if (!validateAuthentication(nodeExecution)) {
            nodeExecution.setStatus(ExecutionStatus.FAILED);
            nodeExecution.setMessage(ResponseCode.AUTH_FAILED.info());
            flowExecution.setResponseCode(ResponseCode.AUTH_FAILED);
            return;
        }
        super.invoke(nodeExecution, flowExecution);
    }

    /*
    @Override
    public void invoke(NodeExecution nodeExecution) {
        List<ExecutionInput> inputs = nodeExecution.getInputs();
        NodeOutput nodeOutput = new NodeOutput();
        for (ExecutionInput input : inputs) {
            for (Map<String, Object> item : input.getData()) {
                nodeOutput.addResult(item);
            }
            if (CollectionUtils.isNotEmpty(input.getResources())) {
                List<ExecutionResource> outputResources = IExecutionContextOperator.saveExecutionResources(input.getResources(), nodeExecution.getContextDataPath());
                for (int i = 0; i < outputResources.size(); i++) {
                    nodeOutput.addResource(i,outputResources.get(i));
                }
            }
        }
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
    }
     */

    private boolean validateAuthentication(NodeExecution nodeExecution) {
        if(flowWebhookConfig.getAuthType() == FlowWebhookConfig.AuthType.NONE){
            return true;
        }
        Integer revision = nodeExecution.getRevision();
        if (revision <= 0) {
            return true;
        }

        boolean flag = false;
        List<ExecutionInput> inputs = nodeExecution.getInputs();
        for (ExecutionInput input : inputs) {
            for (Map<String, Object> item : input.getData()) {
                Map<String,Object> headerMap = (Map<String, Object>) item.get("headers");
                if (MapUtils.isEmpty(headerMap)) {
                    continue;
                }
                if(!headerMap.containsKey(Header.AUTHORIZATION.getValue())){
                    return false;
                }
                String value = headerMap.get("Authorization").toString();
                if(flowWebhookConfig.getAuthType() == FlowWebhookConfig.AuthType.BASIC_AUTH){
                    String username = flowWebhookConfig.username();
                    String password = flowWebhookConfig.password();
                    flag = StringUtils.equalsIgnoreCase("Basic " + Base64.encode(username + ":" + password), value);
                    break;
                }else if(flowWebhookConfig.getAuthType() == FlowWebhookConfig.AuthType.BEARER_AUTH){
                    flag = StringUtils.equalsIgnoreCase("Bearer " + flowWebhookConfig.token(), value);
                    break;
                }
            }
        }
        return flag;
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
