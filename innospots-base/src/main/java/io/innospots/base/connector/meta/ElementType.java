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

package io.innospots.base.connector.meta;

/**
 * @author Raydian
 * @date 2020/11/28
 */
public enum ElementType {

    /**
     * form element type
     */

    INPUT("input"),
    PASSWORD("password"),
    INPUT_NUMBER("number"),
    NUMBER("number"),
    TEXTAREA("textarea"),
    RADIO("radio"),
    CHECKBOX("checkbox"),
    SWITCH("switch"),
    SELECT("select"),
    DATE_TIME_PICKER("time picker"),
    TABS("tabs");

    private String desc;

    ElementType(String desc) {
        this.desc = desc;
    }

    public String getDesc() {
        return desc;
    }
}
