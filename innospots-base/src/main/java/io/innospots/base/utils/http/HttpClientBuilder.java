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

package io.innospots.base.utils.http;

import cn.hutool.http.HttpStatus;
import io.innospots.base.connector.http.HttpData;
import io.innospots.base.exception.ValidatorException;
import io.innospots.base.json.JSONUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.hc.client5.http.classic.methods.*;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.entity.UrlEncodedFormEntity;
import org.apache.hc.client5.http.impl.DefaultConnectionKeepAliveStrategy;
import org.apache.hc.client5.http.impl.classic.BasicHttpClientResponseHandler;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.*;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.http.message.BasicHeader;
import org.apache.hc.core5.http.message.BasicNameValuePair;
import org.apache.hc.core5.http.protocol.HttpContext;
import org.apache.hc.core5.util.Timeout;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.*;


/**
 * @author Raydian
 * @date 2021/1/10
 */
public class HttpClientBuilder {


    public static final String APPLICATION_JSON = "application/json";

    public static final String APPLICATION_FORM = "application/x-www-form-urlencoded";

    public static final String CONTENT_ENCODING = "UTF-8";

    private static final Logger logger = LoggerFactory.getLogger(HttpClientBuilder.class);

    private static CloseableHttpClient defaultHttpClient;


    public static CloseableHttpClient build(int timeout, int maxTotal) {
        return build(timeout, maxTotal, null);
    }

    public static CloseableHttpClient build(int timeout, int maxTotal, Map<String, String> defaultHeaders) {
        List<Header> headers = new ArrayList<>();
        if (defaultHeaders != null) {
            defaultHeaders.forEach((k, v) -> {
                headers.add(new BasicHeader(k, v));
            });
        }
        org.apache.hc.client5.http.impl.classic.HttpClientBuilder clientBuilder = HttpClients.custom()
                .setConnectionManager(httpClientConnectionManager(maxTotal))
                .setDefaultRequestConfig(requestConfig(timeout))
                .setKeepAliveStrategy(new DefaultConnectionKeepAliveStrategy());
        if (!headers.isEmpty()) {
            clientBuilder.setDefaultHeaders(headers);
        }
        return clientBuilder.build();
    }


    private static PoolingHttpClientConnectionManager httpClientConnectionManager(int maxTotal) {
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager();
        //max connection size
        cm.setMaxTotal(maxTotal * 3);
        /**
         * max route connection
         */
        cm.setDefaultMaxPerRoute(maxTotal);
        return cm;
    }

    private static RequestConfig requestConfig(Integer maxTimeOut) {
        RequestConfig.Builder configBuilder = RequestConfig.custom();
        // http connection timeout , millisecond
        configBuilder.setConnectionRequestTimeout(Timeout.ofMicroseconds(maxTimeOut));
        configBuilder.setResponseTimeout(Timeout.ofMicroseconds(maxTimeOut));
        return configBuilder.build();
    }

    public static HttpData doGet(CloseableHttpClient httpClient, String url, Map<String, Object> params, Map<String, String> headers) {
        return doGet(httpClient, url, params, headers, null);
    }

    /**
     * http get
     *
     * @param url    url path
     * @param params params
     * @return
     */
    public static HttpData doGet(CloseableHttpClient httpClient, String url, Map<String, Object> params, Map<String, String> headers, HttpContext httpContext) {
        HttpData httpData = new HttpData();
        StringBuffer param = new StringBuffer();
        if (MapUtils.isNotEmpty(params)) {
            httpData.getParams().putAll(params);
            url = dualPathVar(url, params);
            if(!url.contains("?")){
                url += "?";
            }
            int i = 0;
            for (String key : params.keySet()) {
                if (i == 0) {
                    if(!url.endsWith("?")){
                        param.append("&");
                    }
                } else {
                    param.append("&");
                }
                param.append(key).append("=");
                toStringParams(param, params.get(key));
                i++;
            }
            url += param;
        }

        HttpEntity entity = null;
        try {
            HttpGet httpGet = new HttpGet(url);
            if (MapUtils.isNotEmpty(headers)) {
                httpData.getHeaders().putAll(headers);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpGet.setHeader(entry.getKey(), entry.getValue());
                }
            }
            BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();
            String response = httpContext != null ? httpClient.execute(httpGet, httpContext,responseHandler) : httpClient.execute(httpGet,responseHandler);
            fillResponse(httpData, response);

        } catch (IOException | ParseException e) {
            logger.error(e.getMessage(), e);
            httpData.setStatus(HttpStatus.HTTP_INTERNAL_ERROR);
            httpData.setMessage(e.getMessage());
        }
        return httpData;
    }

    private static void fillResponse(HttpData httpData, String resStr) throws IOException, ParseException {
//        int statusCode = response.getCode();
//        httpData.setStatus(statusCode);
//        httpData.setMessage(response.getReasonPhrase());
//        HttpEntity entity = response.getEntity();
//        for (Header header : response.getHeaders()) {
//            httpData.addHeader(header.getName(), header.getValue());
//        }
        String result = null;
        if (result.startsWith("[")) {
            httpData.setBody(JSONUtils.toList(result, LinkedHashMap.class));
        } else if (result.startsWith("{")) {
            httpData.setBody(JSONUtils.toMap(result));
        } else {
            httpData.setBody(result);
        }
//        if (entity != null) {
//            result = EntityUtils.toString(entity, CONTENT_ENCODING);
//            String contentType = entity.getContentType();
//            httpData.addHeader(CONTENT_LENGTH, entity.getContentLength());
//            if (contentType != null && contentType.contains("application/json")) {

//            } else {
//                httpData.setBody(result);
//            }

//        }

//        return entity;
    }

    public static void toStringParams(StringBuffer buffer, Object v) {
        //字符串需要处理
        if (v instanceof String) {
            if (((String) v).contains("\"") || ((String) v).contains(" ")) {
                try {
                    buffer.append(URLEncoder.encode(v.toString(), "utf-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            } else {
                buffer.append(v.toString());
            }
        } else if (v != null) {
            buffer.append(v.toString());
        }
    }

    /**
     * Post请求
     *
     * @param url    请求地址
     * @param params 参数数据
     * @return
     */
    public static HttpData doPost(CloseableHttpClient httpClient,
                                  String url,
                                  Map<String, Object> query,
                                  Map<String, Object> params,
                                  Map<String, String> headers) {
        HttpEntity httpEntity = null;
        HttpData httpData = new HttpData();
        url = dualQueryVar(url, query);
        HttpPost httpPost = new HttpPost(url);
        if(query != null){
            httpData.getParams().putAll(query);
        }
        if(params!=null){
            httpData.getParams().putAll(params);
        }
        try {
            List<NameValuePair> pairList = new ArrayList<>(params.size());
            for (Map.Entry<String, Object> entry : params.entrySet()) {
                NameValuePair pair = new BasicNameValuePair(entry.getKey(), entry
                        .getValue().toString());
                pairList.add(pair);
            }
            if (MapUtils.isNotEmpty(headers)) {
                httpData.getHeaders().putAll(headers);
                for (Map.Entry<String, String> entry : headers.entrySet()) {
                    httpPost.setHeader(entry.getKey(), entry.getValue());
                }
            }

            BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();
            httpPost.setEntity(new UrlEncodedFormEntity(pairList, Charset.forName(CONTENT_ENCODING)));
            String s = httpClient.execute(httpPost,responseHandler);
            fillResponse(httpData, s);
        } catch (
                IOException | ParseException e) {
            httpData.setStatus(HttpStatus.HTTP_INTERNAL_ERROR);
            logger.error(e.getMessage(), e);
        }
        return httpData;
    }


    public static HttpData doPost(CloseableHttpClient httpClient,
                                  String url, Map<String, String> headers,
                                  Map<String, Object> params, String requestBody) {
        return doPost(httpClient, url, headers, params, requestBody, null);
    }

    /**
     * Post 请求
     *
     * @param url         请求地址
     * @param requestBody 参数数据
     * @return
     */
    public static HttpData doPost(CloseableHttpClient httpClient,
                                  String url, Map<String, String> headers,
                                  Map<String, Object> params,
                                  String requestBody,
                                  HttpContext httpContext) {
        HttpData httpData = new HttpData();
        StringBuffer param = new StringBuffer();
        url = dualPathVar(url, params);
        url = dualQueryVar(url, params);
        httpData.getParams().putAll(params);

        HttpPost httpPost = new HttpPost(url);
        if (MapUtils.isNotEmpty(headers)) {
            httpData.getHeaders().putAll(headers);
            for (Map.Entry<String, String> entry : headers.entrySet()) {
                httpPost.setHeader(entry.getKey(), entry.getValue());
            }
        }

        HttpEntity httpEntity = null;
        try {
            if (requestBody != null) {
                StringEntity stringEntity = new StringEntity(requestBody,
                        ContentType.APPLICATION_JSON, CONTENT_ENCODING,true);

                httpPost.setEntity(stringEntity);
            }
            BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();
            String respStr = httpContext != null ?
                    httpClient.execute(httpPost, httpContext, responseHandler) : httpClient.execute(httpPost,responseHandler);
            if (logger.isDebugEnabled()) {
                StringBuilder out = new StringBuilder();
                out.append("url: ").append(url).append(" ,header: ").append(headers).append(" ,param: ").append(param)
                        .append(", body: ").append(requestBody);
            }
            fillResponse(httpData, respStr);
        } catch (IOException | ParseException e) {
            httpData.setMessage(e.getMessage());
            httpData.setStatus(HttpStatus.HTTP_INTERNAL_ERROR);
            logger.error(e.getMessage(), e);
        } finally {
        }

        return httpData;
    }

    /**
     * Post 请求
     *
     * @param url         请求地址
     * @param requestBody 参数数据
     * @return
     */
    public static String doRequest(CloseableHttpClient httpClient, HttpUriRequestBase method, String url, Map<String, Object> params, String requestBody) throws IOException, ParseException {
        String httpStr = null;
        url = dualPathVar(url, params);

        HttpEntity httpEntity = null;
        try {
            //encode character
            if (requestBody != null) {
                StringEntity stringEntity = new StringEntity(requestBody,ContentType.APPLICATION_JSON, CONTENT_ENCODING,true);
                method.setEntity(stringEntity);
            }
            BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();
            httpStr = httpClient.execute(method,responseHandler);
        } catch (IOException e) {
            throw e;
        }
        return httpStr;
    }

    /**
     * Post 请求
     *
     * @param url         请求地址
     * @param requestBody 参数数据
     * @return
     */
    public static String deleteHttpRequest(CloseableHttpClient httpClient, String url, Map<String, Object> params, String requestBody) throws IOException, ParseException {
        String httpStr = null;
        url = dualPathVar(url, params);
        HttpUriRequestBase method = new HttpDelete(url);
        HttpEntity httpEntity = null;
        try {
            BasicHttpClientResponseHandler responseHandler = new BasicHttpClientResponseHandler();
            httpStr = httpClient.execute(method,responseHandler);
        } catch (IOException e) {
            throw e;
        }
        return httpStr;
    }

    public static String dualPathVar(String url, Map<String, Object> params) {
        if (MapUtils.isEmpty(params)) {
            return url;
        }
        Iterator<Map.Entry<String, Object>> iterator = params.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Object> item = iterator.next();
            String pathKey = "{" + item.getKey() + "}";
            if (url.contains(pathKey)) {
                url = url.replace(pathKey, String.valueOf(item.getValue()));
                iterator.remove();
            }
        }
        return url;
    }

    public static String dualQueryVar(String url, Map<String, Object> query) {
        StringBuffer param = new StringBuffer();
        if (query != null && query.size() > 0) {
            int i = 0;
            for (String key : query.keySet()) {
                if (i == 0) {
                    param.append("?");
                } else {
                    param.append("&");
                }
                param.append(key).append("=");
                toStringParams(param, query.get(key));
                i++;
            }
            url += param;
        }
        return url;
    }

    public static void fillBasicAuthHeader(String username, String password, Map<String, String> headers, Charset charset) {
        if (headers == null) {
            throw ValidatorException.buildInvalidException(HttpClientBuilder.class, "headers can not be null");
        }
        final String data = username.concat(":").concat(password);
        headers.put(HttpHeaders.AUTHORIZATION, "Basic " + Base64.getEncoder().encodeToString(data.getBytes(charset)));
    }

    public static void fillBearerAuthHeader(String token, Map<String, String> headers) {
        if (headers == null) {
            throw ValidatorException.buildInvalidException(HttpClientBuilder.class, "headers can not be null");
        }
        headers.put(HttpHeaders.AUTHORIZATION, "Bearer " + token);
    }

    public static String post(String url, Map<String, Object> params, String requestBody) throws IOException {
        if (defaultHttpClient == null) {
            defaultHttpClient = build(15 * 1000, 20);
        }
        return doPost(defaultHttpClient, url, null, params, requestBody, null).getBody().toString();
    }

    public static String get(String url, Map<String, Object> param, Map<String, String> header) throws IOException {
        if (defaultHttpClient == null) {
            defaultHttpClient = build(15 * 1000, 20);
        }
        return doGet(defaultHttpClient, url, param, header).getBody().toString();
    }

    public static String put(String path, Map<String, Object> params, String requestBody) throws IOException, ParseException {
        HttpUriRequestBase method = new HttpPut(path);
        if (defaultHttpClient == null) {
            defaultHttpClient = build(15 * 1000, 20);
        }
        return doRequest(defaultHttpClient, method, path, params, requestBody);
    }

    public static String delete(String path, Map<String, Object> params, String requestBody) throws IOException, ParseException {

        if (defaultHttpClient == null) {
            defaultHttpClient = build(15 * 1000, 20);
        }
        return deleteHttpRequest(defaultHttpClient, path, params, requestBody);
    }

}
