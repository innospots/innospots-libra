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

import cn.hutool.core.io.FileUtil;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.base.Enums;
import io.innospots.base.condition.Factor;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.PlaceholderUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.noear.snack.ONode;

import java.io.File;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class ApiDataNode extends DataNode {

    public static final String FIELD_PAYLOAD = "output_payload";
    public static final String FIELD_JSON_PATH = "extract_path";

    public static final String FIELD_REQUEST_PARAMS = "request_params";
    public static final String FIELD_PRE_ACTION = "pre_action";
    public static final String FIELD_SWITCH_CACHE = "switch_cache";
    public static final String FIELD_CACHE_SECOND = "cache_second";

    public static final String FIELD_URL = "url_address";


    /**
     * output to flow execution contexts
     */
    private boolean outputPayload;

    private String extractJsonPath;

    private String urlAddress;

    private String operation;

    private RequestParam requestParam;

    private boolean dataCache;

    private Cache<String, DataBody> dataBodyCache;

    @Override
    protected void initialize() {
        super.initialize();
        this.outputPayload = valueBoolean(FIELD_PAYLOAD);
        if (this.outputPayload) {
            fillOutputConfig();
            this.extractJsonPath = validString(FIELD_JSON_PATH);
            dataCache = valueBoolean(FIELD_SWITCH_CACHE);
            if (dataCache) {
                dataBodyCache = Caffeine.newBuilder().expireAfterWrite(
                        valueInteger(FIELD_CACHE_SECOND), TimeUnit.SECONDS).build();
            }
        }
        Object urlInfo = value(FIELD_URL);
        if (urlInfo instanceof List) {
            operation = (String) ((List<?>) urlInfo).get(0);
            urlAddress = (String) ((List<?>) urlInfo).get(1);
        }

        if (credentialKey != null) {
            IDataConnectionMinder connectorMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        }

        List<Map<String, Object>> fieldParams = valueMapList(FIELD_REQUEST_PARAMS);

        if (CollectionUtils.isNotEmpty(fieldParams)) {
            requestParam = new RequestParam();
            for (Map<String, Object> fieldParam : fieldParams) {
                String type = String.valueOf(fieldParam.get("type"));
                ParamType paramType = Enums.getIfPresent(ParamType.class, type).orNull();
                Object values = fieldParam.get("values");
                if (paramType == null || values == null) {
                    log.warn("request params not config correctly,paramType: {}, values: {}", paramType, values);
                    continue;
                }
                switch (paramType) {
                    case header:
                        if (values instanceof List) {
                            List<Factor> factors = BeanUtils.toBean((List<Map<String, Object>>) values, Factor.class);
                            requestParam.headers = factors.stream().filter(f -> StringUtils.isNotEmpty(f.getName())).collect(Collectors.toList());
                        }
                        break;
                    case body:
                        if (values instanceof List) {
                            List<Factor> factors = BeanUtils.toBean((List<Map<String, Object>>) values, Factor.class);
                            requestParam.body = factors.stream().filter(f -> StringUtils.isNotEmpty(f.getName())).collect(Collectors.toList());
                        }
                        break;
                    case query:
                        if (values instanceof List) {
                            List<Factor> factors = BeanUtils.toBean((List<Map<String, Object>>) values, Factor.class);
                            requestParam.query = factors.stream().filter(f -> StringUtils.isNotEmpty(f.getName())).collect(Collectors.toList());
                        }
                        break;
                    case template:
                        requestParam.template = String.valueOf(values);
                        break;
                    default:
                        break;
                }
            }

        }

    }


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        //Map<String, String> resp = new HashMap<>();
        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            List<Map<String, Object>> items = joinData(nodeExecution);
            for (Map<String, Object> item : items) {
                request(nodeOutput, item);
            }
        } else {
            request(nodeOutput, new LinkedHashMap<>());
        }
    }

    private List<Map<String, Object>> joinData(NodeExecution nodeExecution) {
        if (nodeExecution.getInputs().size() > 1) {
            List<Map<String, Object>> ll = new ArrayList<>();
            ExecutionInput mainInput = nodeExecution.getInputs().get(0);
            ExecutionInput joinInput = nodeExecution.getInputs().get(1);
            for (Map<String, Object> mainData : mainInput.getData()) {
                for (Map<String, Object> secondData : joinInput.getData()) {
                    LinkedHashMap<String, Object> data = new LinkedHashMap<>(mainData);
                    data.putAll(secondData);
                    ll.add(data);
                }
            }
            return ll;
        } else if (nodeExecution.getInputs().get(0) != null) {
            return nodeExecution.getInputs().get(0).getData();
        } else {
            return Collections.emptyList();
        }
    }

    private void request(NodeOutput nodeOutput, Map<String, Object> item) {
        item = prevExecute(item);
        DataBody httpResponse = doRequest(item);
        if (!outputPayload) {
            fillOutput(nodeOutput, item);
            return;
        }
        if (httpResponse!=null) {
            fillOutput(nodeOutput, item);
            return;
        }
        Object body = httpResponse.getBody();
//        if (this.expression != null) {
//            body = this.expression.execute(JSONUtils.objectToMap(body));
//        }
        body = extract(body);
        fillOutput(nodeOutput, item, body);
    }

    private Map<String, Object> prevExecute(Map<String, Object> item) {
        /*
        if (this.actionScripts == null) {
            return item;
        }
        IExpression exp = this.actionScripts.get(FIELD_PRE_ACTION);
        if (exp == null) {
            return item;
        }
        return (Map<String, Object>) exp.execute(item);
         */
        return item;
    }


    private Object extract(Object value) {
        Object v = null;
        if (value instanceof List) {
            v = value;
        } else if (value instanceof Map) {
            v = value;
        } else {
            v = JSONUtils.objectToMap(value);
        }

        if (this.extractJsonPath == null || "$".equals(this.extractJsonPath)) {
            return v;
        }

        if (this.outputFieldType == OutputFieldType.MAP) {
            ONode jsonNode = ONode.load(v);
            value = jsonNode.select(this.extractJsonPath).toObject();
        } else {
            value = ONode.load(v).select(this.extractJsonPath).toObjectList(Map.class);
        }
        return value;
    }

    private DataBody doRequest(Map<String, Object> item) {
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.setCredentialKey(credentialKey);
        itemRequest.setOperation(operation);
        itemRequest.setUri(urlAddress);
        itemRequest.setConnectorName("Http");
        if (requestParam != null) {
            requestParam.fill(item, itemRequest);
        }
        DataBody dataBody = null;
        if (dataCache) {
            dataBody = dataBodyCache.getIfPresent(itemRequest.key());
        }
        if (dataBody == null) {
            dataBody = dataOperator.execute(itemRequest);
            if (dataCache) {
                dataBodyCache.put(itemRequest.key(), dataBody);
            }
        }

        return dataBody;
    }


    private enum ParamType {
        /**
         *
         */
        header,
        query,
        body,
        template;

    }

    @Getter
    @Setter
    private class RequestParam {
        private List<Factor> headers;
        private List<Factor> query;
        private List<Factor> body;
        private String template;


        public void fill(Map<String, Object> item, BaseRequest itemRequest) {
            if (CollectionUtils.isNotEmpty(headers)) {
                for (Factor factor : headers) {
                    Object v = factor.value(item);
                    if (v == null) {
                        continue;
                    }
                    itemRequest.addHeader(factor.getName(), v);
                }
            }//end if header empty
            if (CollectionUtils.isNotEmpty(query)) {
                for (Factor factor : query) {
                    Object v = factor.value(item);
                    if (v == null) {
                        v = factor.getValue();
                    }
                    if(v==null){
                        continue;
                    }
                    if (StringUtils.isNotEmpty(itemRequest.getUri())) {
                        String ph = "${" + factor.getName() + "}";
                        if (itemRequest.getUri().contains(ph)) {
                            itemRequest.setUri(itemRequest.getUri().replace(ph, v.toString()));
                            continue;
                        }
                    }//end uri not empty
                    itemRequest.addQuery(factor.getName(), v);
                }//end for query
            }//end if query not empty
            Map<String, String> bValue = new LinkedHashMap<>(5);
            if (CollectionUtils.isNotEmpty(body)) {
                for (Factor factor : body) {
                    Object value = null;
                    if("file".equalsIgnoreCase(factor.getCode())){
                        value = extractFileBase64(factor,item);
                    }else{
                        value = factor.value(item);
                    }
                    if (value == null) {
                        continue;
                    }
                    bValue.put(factor.getName(), String.valueOf(value));
                }//end for body
            }//end if body not empty
            if (StringUtils.isNotEmpty(template) && bValue.size() > 0) {
                itemRequest.setBody(PlaceholderUtils.replacePlaceholders(template, bValue));
            } else if (bValue.size() > 0) {
                itemRequest.setBody(bValue);
            }//end if template

        }

        private String extractFileBase64(Factor factor, Map<String, Object> item){
            Object value = getFileValue(factor,item);
            if(value == null){
                return null;
            }
            String fileValue = String.valueOf(value);
            if(fileValue.startsWith("file://")){
                fileValue = fileValue.replace("file://","");
                String filePath = EncryptorBuilder.encryptor.decode(fileValue);
                File file = new File(filePath);
                if(!file.exists()){
                    return null;
                }
                byte[] fileBytes = FileUtil.readBytes(file);
                String base64Content = Base64.encodeBase64String(fileBytes);
                if(log.isDebugEnabled()){
                    log.debug("file base64 size:{}, {}",base64Content.length(),factor.getName());
                }
                return base64Content;
            }//end file if
            return fileValue;
        }

        private Object getFileValue(Factor factor, Map<String, Object> item) {
            String valueKey = factor.valueKey();
            String[] vs = valueKey.split("[.]");
            if (vs.length == 2) {
                Object m = item.get(vs[0]);
                if (m instanceof Map) {
                    return ((Map<?, ?>) m).get(vs[1]);
                } else {
                    return m;
                }
            } else {
                return item.get(valueKey);
            }
        }

    }


}
