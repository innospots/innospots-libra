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

package io.innospots.server.base.registry.enums;

import lombok.extern.slf4j.Slf4j;

/**
 * @author Raydian
 * @date 2020/11/29
 */
@Slf4j
public enum ServiceType {

    /**
     *
     */
    ADMINISTRATION("administration", "leader"),
    SCHEDULER("scheduler", "leader"),
    DATA("data", "cluster"),
    WORKFLOW("workflow", "leader"),
    EXECUTOR("executor", "cluster");

    private String desc;

    private String haMode;

    ServiceType(String desc, String haMode) {
        this.desc = desc;
        this.haMode = haMode;
    }

    public String desc() {
        return desc;
    }

    public static ServiceType getServiceType(String name) {
        for (ServiceType item : values()) {
            if (item.desc().equals(name)) {
                return item;
            }
        }
        log.error("not exist serviceType:{}", name);
        return null;
    }
}
