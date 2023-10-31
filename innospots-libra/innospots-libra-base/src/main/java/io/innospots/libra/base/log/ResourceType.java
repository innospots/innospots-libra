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

package io.innospots.libra.base.log;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/3
 */
public enum ResourceType {

    /**
     *
     */
    Datasource("datasource"),
    DataSet("datasource"),
    SchemaRegistry("datasource"),
    SchemaField("datasource"),
    DataSetCategory("datasource"),
    Role("libra"),
    UserRole("libra"),
    User("libra"),
    Page("page"),
    PageCategory("page"),
    Notice("libra"),
    Menu("libra"),
    SysConfig("libra"),
    Workflow("workflow"),
    WorkflowCategory("workflow"),
    Credential("credential"),
    Extension("extension");

    private String module;


    ResourceType(String module) {
        this.module = module;
    }

    public String getModule() {
        return module;
    }

}
