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

package io.innospots.workflow.runtime.config;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.constant.ServiceConstant;
import io.innospots.base.utils.BeanContextAwareUtils;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Smars
 * @date 2021/7/3
 */
@Setter
@Getter
@ConfigurationProperties(prefix = "innospots.workflow.runtime")
public class WorkflowRuntimeProperties {

    /**
     * 是否开启节点存储
     */
    private boolean enableNodeStore;

    /**
     * 是否开启执行记录存储
     */
    private boolean enableContextStore;

    /**
     *
     */
    private int queueThreadCapacity = 500;

    private int maxCycleFlow = 50;

    private boolean debugOutput = true;

    private Integer port;

    private String host;

    public static String serverAddress(){
        String host = null;
        Integer port = null;
        try{
            WorkflowRuntimeProperties runtimeProperties = BeanContextAwareUtils.getBean(WorkflowRuntimeProperties.class);
            host = runtimeProperties.getHost();
            port = runtimeProperties.port;
        }catch (Exception e){

        }
        if(host == null){
            host = BeanContextAwareUtils.serverIpAddress();
        }
        if(port == null){
            port = BeanContextAwareUtils.serverPort();
        }
        return host+ ":" + port;
    }

    public static String webHookApi(){
        return ServiceConstant.webHookApi(serverAddress());
    }

    public static String webHookApiTest(){
        return ServiceConstant.webHookApiTest(serverAddress());
    }

}
