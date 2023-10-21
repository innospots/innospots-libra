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

package io.innospots.base.data.http;

import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.RequestBody;
import io.innospots.base.utils.HttpClientBuilder;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.protocol.HttpContext;

import java.util.Map;


/**
 * @author Alfred
 * @date 2021-08-21
 */
public class HttpDataExecutor implements IExecutionOperator, HttpConstant {


    private HttpConnection httpConnection;


    private HttpContext httpContext;


    public HttpDataExecutor(HttpConnection httpConnection) {
        this.httpConnection = httpConnection;
        //fillExpression();
    }

    public HttpDataExecutor(HttpConnection httpConnection, HttpContext context) {
        this.httpConnection = httpConnection;
        this.httpContext = context;
    }


    @Override
    public DataBody<?> execute(RequestBody requestBody) {
        String url = requestBody.getUri();
        if (url == null) {
            url = httpConnection.connectionCredential().v(HTTP_API_URL);
        }

        HttpData data = null;
        if (ApiMethod.POST.equals(ApiMethod.valueOf(requestBody.getOperation()))) {
            if (requestBody.getHeaders() != null &&
                    HttpClientBuilder.APPLICATION_FORM.equals(requestBody.getHeaders().get(HEADER_CONTENT_TYPE))) {
                data = httpConnection.postForm(url, requestBody.getQuery(), requestBody.getBody(), requestBody.getHeaders());

            } else {
                String cnt = requestBody.getContent();
                if (StringUtils.isNotEmpty(cnt)) {
                    data = httpConnection.post(url, requestBody.getQuery(),
                            JSONUtils.toMap(cnt), requestBody.getHeaders(), httpContext);
                } else {
                    data = httpConnection.post(url, requestBody.getQuery(),
                            requestBody.getBody(), requestBody.getHeaders(), httpContext);
                }
            }
        } else if (ApiMethod.GET.equals(ApiMethod.valueOf(requestBody.getOperation()))) {
            data = httpConnection.get(url, requestBody.getQuery(), requestBody.getHeaders());
        } else {
            throw ValidatorException.buildInvalidException(this.getClass(), "httpMethod invalid", requestBody.getOperation());
        }

        DataBody<HttpData> dataBody = new DataBody<>();
        dataBody.setBody(data);
        return dataBody;
    }


    public static String bodyTemplate(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_BODY_TEMPLATE));
    }

    public static String preScript(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_PREV_SCRIPT));
    }

    public static String postScript(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_POST_SCRIPT));
    }

    public static ApiMethod httpMethod(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return ApiMethod.valueOf(String.valueOf(configs.get(HTTP_METHOD)));
    }

    public static String url(Map<String, Object> configs) {
        if (configs == null) {
            return null;
        }
        return String.valueOf(configs.get(HTTP_API_URL));
    }


}
