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

package io.innospots.workflow.core.enums;


/**
 * @author Wren
 * @date 2021/05/11
 */

public enum FlowVersion {

    /**
     * flow version
     */
    DRAFT(0, "draft"),
    PUBLISH(Integer.MAX_VALUE, "publish");

    private Integer version;
    private String info;

    FlowVersion(Integer version, String info) {
        this.version = version;
        this.info = info;
    }

    public static String getVersionInfo(Integer version) {
        for (FlowVersion flowVersion : FlowVersion.values()) {
            if (flowVersion.getVersion().equals(version)) {
                return flowVersion.getInfo();
            }
        }
        return null;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }
}
