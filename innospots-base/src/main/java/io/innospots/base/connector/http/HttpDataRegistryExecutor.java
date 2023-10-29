/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.base.connector.http;

import io.innospots.base.connector.schema.ApiSchemaRegistry;
import io.innospots.base.connector.schema.SchemaField;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.exception.data.HttpConnectionException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.re.GenericExpressionEngine;
import io.innospots.base.re.IExpression;
import io.innospots.base.utils.PlaceholderUtils;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpStatus;
import org.apache.http.protocol.HttpContext;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 * @author Alfred
 * @date 2021-08-21
 */
@Deprecated
public class HttpDataRegistryExecutor implements IExecutionOperator {


    private final HttpConnection httpConnection;

    private final ApiSchemaRegistry schemaRegistry;

    private HttpContext httpContext;

    private IExpression preScriptExpression;

    private IExpression postScriptExpression;

    public HttpDataRegistryExecutor(HttpConnection httpConnection, ApiSchemaRegistry schemaRegistry) {
        this.httpConnection = httpConnection;
        this.schemaRegistry = schemaRegistry;
        fillExpression();
    }

    public HttpDataRegistryExecutor(HttpConnection httpConnection, ApiSchemaRegistry schemaRegistry, HttpContext context) {
        this.httpConnection = httpConnection;
        this.schemaRegistry = schemaRegistry;
        this.httpContext = context;
        fillExpression();
    }


    @Override
    public void open() {

    }

    @Override
    public DataBody<?> execute(ItemRequest itemRequest) {
        Map<String, Object> body = itemRequest.getBody();
        String url = this.schemaRegistry.getAddress();

        String httpBodyTemplate = this.schemaRegistry.getBodyTemplate();

        List<SchemaField> schemaFields = schemaRegistry.getSchemaFields();

        Map<FieldScope, List<SchemaField>> dataFieldMap;
        if (CollectionUtils.isEmpty(schemaFields)) {
            dataFieldMap = Collections.emptyMap();
        } else {
            dataFieldMap = schemaFields.stream().collect(Collectors.groupingBy(SchemaField::getFieldScope));
        }

        List<SchemaField> headerFields = dataFieldMap.get(FieldScope.HTTP_HEADER);

        List<SchemaField> pathFields = dataFieldMap.get(FieldScope.HTTP_PATH);

        List<SchemaField> bodyFields = dataFieldMap.get(FieldScope.HTTP_BODY);

        List<SchemaField> paramFields = dataFieldMap.get(FieldScope.HTTP_PARAM);

        if (this.preScriptExpression != null) {
            Map<String, Object> preScriptResultMap = (Map<String, Object>) this.preScriptExpression.execute(body);
            if (preScriptResultMap != null) {
                body.putAll(preScriptResultMap);
            }
        }

        Map<String, String> headers = extractStrFields(headerFields, body);
        Map<String, String> pathValues = extractStrFields(pathFields, body);
        Map<String, Object> requestParams = extractFields(paramFields, body);
        Map<String, Object> bodyValues = extractBody(bodyFields, body, httpBodyTemplate);

        url = PlaceholderUtils.replacePlaceholders(url, pathValues, true);

        HttpData data = null;
        if (ApiMethod.GET == this.schemaRegistry.getApiMethod()) {
            data = httpConnection.get(url, requestParams, headers);
        } else if (ApiMethod.POST == this.schemaRegistry.getApiMethod()) {
            data = httpConnection.post(url, requestParams, bodyValues, headers, httpContext);
        } else {
            throw ValidatorException.buildInvalidException(this.getClass(), "httpMethod invalid", this.schemaRegistry.getApiMethod());
        }

        if (data.getStatus() != HttpStatus.SC_OK) {
            throw HttpConnectionException.buildException(this.getClass(), itemRequest);
        }

        if (this.postScriptExpression != null && data.getBody() != null) {
            Map<String, Object> dataParams = new HashMap<>();
            dataParams.put("body", data.getBody());
            Map<String, Object> result = (Map<String, Object>) postScriptExpression.execute(dataParams);
            data.setBody(result);
        }

        DataBody<HttpData> dataBody = new DataBody<>();
        dataBody.setBody(data);
        return dataBody;
    }

    @Override
    public void close() {

    }

    private Map<String,Object> extractBody(List<SchemaField> schemaFields, Map<String, Object> body, String httpBodyTemplate) {
        if (CollectionUtils.isEmpty(schemaFields)) {
            return Collections.emptyMap();
        }
        Map<String, String> values = new LinkedHashMap<>();
        for (SchemaField schemaField : schemaFields) {
            values.put(schemaField.getCode(), String.valueOf(body.get(schemaField.getCode())));
        }
        if (httpBodyTemplate != null) {
            httpBodyTemplate = PlaceholderUtils.replacePlaceholders(httpBodyTemplate, values, true);
            return JSONUtils.toMap(httpBodyTemplate);
        }
        return new HashMap<>(values);
    }

    private Map<String, Object> extractFields(List<SchemaField> schemaFields, Map<String, Object> body) {
        if (CollectionUtils.isEmpty(schemaFields)) {
            return Collections.emptyMap();
        }
        Map<String, Object> values = new LinkedHashMap<>();
        for (SchemaField schemaField : schemaFields) {
            values.put(schemaField.getCode(), body.get(schemaField.getCode()));
        }
        return values;
    }

    private Map<String, String> extractStrFields(List<SchemaField> schemaFields, Map<String, Object> body) {
        if (CollectionUtils.isEmpty(schemaFields)) {
            return Collections.emptyMap();
        }

        Map<String, String> values = new LinkedHashMap<>();
        for (SchemaField schemaField : schemaFields) {
            values.put(schemaField.getCode(), String.valueOf(body.get(schemaField.getCode())));
        }
        return values;
    }


    private void fillExpression() {

        String preScript = this.schemaRegistry.getPrevScript();

        String postScript = this.schemaRegistry.getPostScript();

        if (StringUtils.isAllEmpty(preScript, postScript)) {
            return;
        }

        GenericExpressionEngine engine = GenericExpressionEngine.build("HttpApiScript" + "_" + this.schemaRegistry.getCode());
        if (StringUtils.isNotEmpty(preScript)) {
            engine.register(ScriptType.JAVA, Map.class, "preCall", preScript);
        }
        if (StringUtils.isNotEmpty(postScript)) {
            engine.register(ScriptType.JAVA, Map.class, "postCall", preScript);
        }
        engine.compile();
        this.preScriptExpression = engine.getExpression("preCall");
        this.postScriptExpression = engine.getExpression("postCall");
    }


}
