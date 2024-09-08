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


import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.connector.schema.model.ApiSchemaRegistry;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;


/**
 * @author Alfred
 * @date 2021-08-21
 */
@Slf4j
public class HttpDataConnectionMinder extends BaseDataConnectionMinder {

    private static final Logger logger = LoggerFactory.getLogger(HttpDataConnectionMinder.class);

    protected HttpConnection httpConnection;

    protected HttpDataExecutor httpDataExecutor;

    public static final HttpDataConnectionMinder DEFAULT_HTTP_CONNECTION_MINDER = new HttpDataConnectionMinder().initialize();


    protected Supplier<Map<String, String>> headers() {
        return () -> {
            return new HashMap<>();
        };
    }

    protected Supplier<Map<String, Object>> defaultParams() {
        return () -> {
            return new HashMap<>();
        };
    }

    protected Supplier<Map<String, Object>> defaultBody() {
        return () -> {
            return new HashMap<>();
        };
    }

    public HttpDataConnectionMinder initialize() {
        if (this.httpConnection != null) {
            open();
        } else {
            httpConnection = new HttpConnection();
        }
        return this;
    }

    @Override
    public void open() {
        if (httpConnection != null) {
            return;
        }
        httpConnection = new HttpConnection(connectionCredential, headers(), defaultParams(), defaultBody());
        httpDataExecutor = new HttpDataExecutor(httpConnection);
    }


    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        return true;
    }

    @SneakyThrows
    @Override
    public void close() {
        if (this.httpDataExecutor != null) {
            this.httpDataExecutor.close();
        }
    }

    @Override
    public String schemaName() {
        return "api_schema";
    }


    @SneakyThrows
    public Object fetchSample(ConnectionCredential connectionCredential, ApiSchemaRegistry apiSchemaRegistry) {
        HttpConnection httpConnection = new HttpConnection(connectionCredential);
        HttpDataExecutor httpDataExecutor = new HttpDataExecutor(httpConnection);
        ItemRequest itemRequest = new ItemRequest();
        Object body = httpDataExecutor.execute(itemRequest).getBody();
        httpConnection.close();
        return body;
    }

    @SneakyThrows
    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {

        HttpConnection httpConnection = new HttpConnection(connectionCredential);
        HttpDataExecutor httpDataExecutor = new HttpDataExecutor(httpConnection);
        ItemRequest itemRequest = new ItemRequest();
        Object body = httpDataExecutor.execute(itemRequest).getBody();
        httpConnection.close();
        return body;
    }

    @Override
    public IExecutionOperator buildOperator() {
        if (httpDataExecutor == null) {
            httpDataExecutor = new HttpDataExecutor(httpConnection);
        }
        return httpDataExecutor;
    }

}
