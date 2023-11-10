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

package io.innospots.workflow.runtime.server;

import io.innospots.base.exception.BaseException;
import io.innospots.base.exception.ErrorResponse;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.flow.FlowExecutionBase;
import io.innospots.workflow.core.runtime.webhook.WorkflowResponse;
import io.innospots.workflow.runtime.container.WebhookRuntimeContainer;
import io.netty.channel.ChannelOption;
import io.netty.handler.codec.http.HttpResponseStatus;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.NettyOutbound;
import reactor.netty.http.server.HttpServer;
import reactor.netty.http.server.HttpServerRequest;
import reactor.netty.http.server.HttpServerResponse;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/7/14
 */
@Slf4j
public class WorkflowWebhookServer {

    public static final String API_PATH = "/runtime/webhook/";

    private final int port;

    private String host;

    private WebhookRuntimeContainer webhookRuntimeContainer;

    private DisposableServer disposableServer;

    public WorkflowWebhookServer(int port, String host, WebhookRuntimeContainer webhookRuntimeContainer) {
        this.port = port;
        this.host = host;
        this.webhookRuntimeContainer = webhookRuntimeContainer;
    }

    public void start() {
        log.info("start workflow webhook server, {}:{}", host, port);

        disposableServer = HttpServer.create()
                .port(port)
//                .host(host)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000)
                .route(routes ->
                        routes.post(API_PATH + "{path}",
                                        (req, res) ->
                                                res.sendString(postDataString(req, res))
                                )
                                .get(API_PATH + "{path}",
                                        (req, res) ->
                                                res.sendString(getData(req, res))
                                )
                                .post("/echo", (req, res) -> res.send(req.receive().retain()))
                                .post("/data-bytes", (req, res) -> res.sendByteArray(postDataBytes(req)))
                                .post("/data", (req, res) -> res.sendString(postDataString(req, res)))
                                .post("/data-handle", this::handle)
                                .get("/",
                                        (req, res) ->
                                                res.sendString(Mono.just("OK!"))
                                )
                ).bindNow();
    }

    public void stop() {
        log.info("stop workflow webhook server.");
        disposableServer.disposeNow();
    }

    private NettyOutbound handle(HttpServerRequest req, HttpServerResponse res) {
        return res.status(HttpResponseStatus.OK).sendString(Flux.just(""));
    }

    private Flux<byte[]> postDataBytes(HttpServerRequest httpServerRequest) {
        return httpServerRequest.receive().asByteArray().map(
                data -> {
                    log.info(new String(data));
                    return data;
                }
        );
    }

    private void authenticate(HttpServerRequest httpServerRequest) {
        //TODO used coming soon.
    }

    private Flux<String> getData(HttpServerRequest httpServerRequest, HttpServerResponse response) {
        String responseBody = process(httpServerRequest, null);
        return Flux.just(responseBody);
    }

    private Flux<String> postDataString(HttpServerRequest httpServerRequest, HttpServerResponse response) {
        return httpServerRequest.receive().asString().map(
                body -> {
                    if (log.isDebugEnabled()) {
                        log.debug("request body:{}", body);
                    }
                    return process(httpServerRequest, body);
                }
        ).doOnError(e -> {
                    log.error(e.getMessage());
                }
        ).onErrorResume(ex -> {
            //response.status(HttpResponseStatus.INTERNAL_SERVER_ERROR);
            ErrorResponse error = null;
            if (ex instanceof BaseException) {
                error = ErrorResponse.build((BaseException) ex);
            } else {
                error = new ErrorResponse();
                error.setMessage(ex.getMessage());
            }
            return Flux.just(JSONUtils.toJsonString(error));
        });
    }

    private String process(HttpServerRequest httpServerRequest, String body) {
        Map<String, Object> payload = new HashMap<>();
        Map<String, Object> headers = new HashMap<>();
        for (Map.Entry<String, String> requestHeader : httpServerRequest.requestHeaders()) {
            headers.put(requestHeader.getKey(), requestHeader.getValue());
        }
        Map<String, Object> params = new HashMap<>();
        if (httpServerRequest.params() != null) {
            params.putAll(httpServerRequest.params());
        }
        payload.put("headers", headers);
        payload.put("params", params);
        if (log.isDebugEnabled()) {
            log.debug("request:{}", payload);
        }
        if (body != null) {
            payload.put("body", JSONUtils.toMap(body));
        } else {
            payload.put("body", new HashMap<>());
        }
        String flowKey = httpServerRequest.param("path");
        Map<String, Object> context = new HashMap<>(5);
        context.put(FlowExecutionBase.PROP_LOCATION, ip(httpServerRequest));
        context.put(FlowExecutionBase.PROP_URI, httpServerRequest.uri());
        WorkflowResponse response = webhookRuntimeContainer.run(flowKey, payload, context);
        return JSONUtils.toJsonString(response);
    }

    private String ip(HttpServerRequest request) {
        InetSocketAddress socketAddress = request.remoteAddress();
        if (socketAddress != null) {
            InetAddress inetAddress = socketAddress.getAddress();
            if (inetAddress != null) {
                return inetAddress.getHostAddress();
            }
        }
        return null;
    }

    /*
    private Flux<byte[]> dispatch(HttpServerRequest httpServerRequest,String flowKey){
        Map<String,Object> payload = new HashMap<>();
        Map<String,Object> headers = new HashMap<>();
        for (Map.Entry<String, String> requestHeader : httpServerRequest.requestHeaders()) {
            headers.put(requestHeader.getKey(), requestHeader.getValue());
        }
        Map<String,Object> params = new HashMap<>();
        if(httpServerRequest.params()!=null){
            params.putAll(httpServerRequest.params());
        }
        payload.put("headers",headers);
        payload.put("params",params);
        if(log.isDebugEnabled()){
            log.debug("request:{}",payload);
        }


        return httpServerRequest.receiveContent().map(httpContent -> {

            if(log.isDebugEnabled()){
                log.debug("httpContent:{}",payload);
            }
//            payload.put("body",JSONUtils.toMap(new String(httpContent.content().array())));

            WorkflowResponse response = webhookRuntimeContainer.run(flowKey,payload);
            return JSONUtils.toJsonString(response).getBytes();
        });

     */

        /*
       return  httpServerRequest.receive()
               .aggregate()
               .asByteArray()
                .doOnError(e -> {
                    log.error(e.getMessage(),e);
                }).map(bytes -> {
                    payload.put("body",JSONUtils.toMap(new String(bytes)));
                    WorkflowResponse response = webhookRuntimeContainer.run(flowKey,payload);
                    return JSONUtils.toJsonString(response).getBytes();
                });

         */
//    }


    public int getPort() {
        return port;
    }
}
