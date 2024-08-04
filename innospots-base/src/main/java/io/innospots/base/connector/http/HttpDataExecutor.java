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

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.data.request.SimpleRequest;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.http.HttpClientBuilder;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.hc.core5.http.protocol.HttpContext;

import java.util.Map;

import static io.innospots.base.connector.http.HttpConstant.HEADER_CONTENT_TYPE;
import static io.innospots.base.connector.http.HttpConstant.HTTP_API_URL;


/**
 * @author Alfred
 * @date 2021-08-21
 */
public class HttpDataExecutor implements IExecutionOperator {


    private final HttpConnection httpConnection;


    private HttpContext httpContext;


    public HttpDataExecutor(HttpConnection httpConnection) {
        this.httpConnection = httpConnection;
    }

    public HttpDataExecutor(HttpConnection httpConnection, HttpContext context) {
        this.httpConnection = httpConnection;
        this.httpContext = context;
    }


    @Override
    public DataBody<?> execute(BaseRequest itemRequest) {
        Object body = itemRequest.getBody();
        String url = itemRequest.getUri();
        if (url == null) {
            url = httpConnection.connectionCredential().v(HTTP_API_URL);
        }

        HttpData data = null;
        if (ApiMethod.POST.equals(ApiMethod.valueOf(itemRequest.getOperation()))) {
            if (itemRequest.getHeaders() != null &&
                    HttpClientBuilder.APPLICATION_FORM.equals(itemRequest.getHeaders().get(HEADER_CONTENT_TYPE))) {
                Map<String,Object> mBody = (Map<String, Object>) body;
                data = httpConnection.postForm(url, itemRequest.getQuery(), mBody, itemRequest.getHeaders());

            } else {
                String cnt = null;
                Map<String,Object> mBody =null;
                if(body instanceof String){
                    cnt = (String) body;
                }else if(body instanceof Map){
                    mBody = (Map<String, Object>) body;
                }
                if (StringUtils.isNotEmpty(cnt)) {
                    data = httpConnection.post(url, itemRequest.getQuery(),
                            JSONUtils.toMap(cnt), itemRequest.getHeaders(), httpContext);
                } else if(mBody != null) {
                    data = httpConnection.post(url, itemRequest.getQuery(),
                            mBody, itemRequest.getHeaders(), httpContext);
                }
            }
        } else if (ApiMethod.GET.equals(ApiMethod.valueOf(itemRequest.getOperation()))) {
            data = httpConnection.get(url, itemRequest.getQuery(), itemRequest.getHeaders());
        } else {
            throw ValidatorException.buildInvalidException(this.getClass(), "httpMethod invalid", itemRequest.getOperation());
        }

        DataBody<HttpData> dataBody = new DataBody<>();
        dataBody.setBody(data);
        return dataBody;
    }

    @Override
    public void open() {

    }

    @SneakyThrows
    @Override
    public void close() {
        httpConnection.close();
    }

}
