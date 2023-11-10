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

package io.innospots.libra.base.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * administration console config
 * @author Raydian
 * @date 2021/1/14
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "innospots.console")
public class InnospotsConsoleProperties {


    /**
     * the days for keeping system login log
     */
    private int sysLoginLogKeepDays = 30;

    /**
     * the max amount for keeping system login log
     */
    private int sysLoginLogKeepAmount = 1000;


    /**
     * enable clean system login log
     */
    private boolean enableCleanSysLoginLog = false;


    /**
     * the days for keeping system operate log
     */
    private int sysOperateLogKeepDays = 30;

    /**
     * the amount for keeping system operation log
     */
    private int sysOperateLogKeepAmount = 1000;

    /**
     * enable clean system operate log
     */
    private boolean enableCleanSysOperateLog = true;



    /**
     * enable swagger api
     */
    private boolean enableSwagger = true;

    /**
     * the directory store upload file
     */
    private String uploadFilePath;

    /**
     * open debug mode
     */
    private boolean debugMode = false;

    /**
     * load extension from classpath
     */
    private boolean extensionLoad = true;


    private Integer schemaCacheTimeoutSecond = 180;

    /**
     * the path of external jar lib
     */
    private String extLibPath;

}
