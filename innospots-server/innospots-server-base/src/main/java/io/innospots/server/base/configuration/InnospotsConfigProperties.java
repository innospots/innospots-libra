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

package io.innospots.server.base.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author Raydian
 * @date 2021/1/14
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.config")
public class InnospotsConfigProperties {


    /**
     * script build path
     */
    private String scriptBuildPath;

    /**
     * log root path
     */
    private String logRootPath;

    /**
     * watcher thread pool size
     */
    private Integer watcherSize = 10;

    /**
     * enable server registry. when start the server, the server info will insert or update into server_registry table
     */
    private boolean enableRegistry = true;



    /**
     * open debug mode
     */
    private boolean debugMode = false;


    private Integer schemaCacheTimeoutSecond = 180;

}
