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

import io.innospots.base.connector.credential.ConnectionCredential;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.http.HttpClientBuilder;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.protocol.HttpContext;

import java.io.Closeable;
import java.io.IOException;
import java.util.Map;
import java.util.function.Supplier;


/**
 * http connection and credential info
 *
 * @author Smars
 * @date 2021/8/31
 */
@Slf4j
public class HttpConnection implements Closeable {

    private final CloseableHttpClient httpClient;

    private final ConnectionCredential connectionCredential;

    private Supplier<Map<String, String>> headersSupplier;

    private Supplier<Map<String, Object>> paramsSupplier;

    private Supplier<Map<String, Object>> bodySupplier;


    public HttpConnection(ConnectionCredential connectionCredential) {
        this(connectionCredential, null);
    }

    public HttpConnection(ConnectionCredential connectionCredential, Map<String, String> headers) {
        this.connectionCredential = connectionCredential;
        this.httpClient = HttpClientBuilder.build(15 * 1000, 15, headers);
    }

    public HttpConnection(ConnectionCredential connectionCredential,
                          Supplier<Map<String, String>> headers,
                          Supplier<Map<String, Object>> params,
                          Supplier<Map<String, Object>> body
    ) {
        this.connectionCredential = connectionCredential;
        this.httpClient = HttpClientBuilder.build(15 * 1000, 15);
        this.headersSupplier = headers;
        this.paramsSupplier = params;
        this.bodySupplier = body;
    }

    public HttpConnection() {
        this(null);
    }


    public ConnectionCredential connectionCredential() {
        return this.connectionCredential;
    }

    public HttpData get(String url, Map<String, Object> params, Map<String, String> headers) {
        return HttpClientBuilder.doGet(httpClient, url, fillParams(params), fillHeader(headers));
    }

    public HttpData post(String url, Map<String, Object> params, String requestBody, Map<String, String> headers) {
        return post(url, params, requestBody, headers, null);
    }


    public HttpData post(String url, Map<String, Object> params, String requestBody, Map<String, String> headers, HttpContext httpContext) {
        return HttpClientBuilder.doPost(httpClient, url, fillHeader(headers), fillParams(params), requestBody, httpContext);
    }

    public HttpData post(String url, Map<String, Object> params, Map<String, Object> jsonBody, Map<String, String> headers, HttpContext httpContext) {
        return HttpClientBuilder.doPost(httpClient, url, fillHeader(headers), fillParams(params), JSONUtils.toJsonString(fillBody(jsonBody)), httpContext);
    }

    public HttpData postForm(String url, Map<String, Object> query, Map<String, Object> params, Map<String, String> headers) {
        return HttpClientBuilder.doPost(httpClient, url, fillBody(query), fillParams(params), fillHeader(headers));
    }

    private Map<String,Object> fillBody(Map<String,Object> body){
        if(this.bodySupplier!=null){
            Map<String,Object> pms = bodySupplier.get();
            if(body==null){
                body = pms;
            }else {
                if(pms!=null){
                    body.putAll(pms);
                }
            }
        }
        return body;
    }

    private Map<String,Object> fillParams(Map<String,Object> params){
        if(this.paramsSupplier!=null){
            Map<String,Object> pms = paramsSupplier.get();
            if(params==null){
                params = pms;
            }else {
                if(pms!=null){
                    params.putAll(pms);
                }
            }
        }
        return params;
    }

    private Map<String,String> fillHeader(Map<String,String> headers){
        if (headersSupplier != null) {
            if (headers == null) {
                headers = headersSupplier.get();
            } else {
                Map<String, String> h2 = headersSupplier.get();
                if (h2 != null) {
                    headers.putAll(h2);
                }
            }
        }
        return headers;
    }

    @Override
    public void close() throws IOException {
        if(this.httpClient!=null){
            this.httpClient.close();
        }
    }
}
