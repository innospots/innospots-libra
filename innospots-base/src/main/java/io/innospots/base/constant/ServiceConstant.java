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

package io.innospots.base.constant;

/**
 * @author Raydian
 * @date 2021/2/6
 */
public interface ServiceConstant {

    String PATH_ROOT_ADMIN_ENDPOINT = "innospots/admin/endpoint";
    String PATH_ROOT_DATA_API = "innospots/data";
    String PATH_ROOT_ISP = "innospots/isp";
    String PATH_ROOT_EXECUTOR = "innospots/executor";

    String SRV_NAME_DATA_SERVER = "innospots-Data-Server";
    String SRV_NAME_ADMIN_CONSOLE = "innospots-Administration-Console";
    String SRV_NAME_ISP_SERVER = "innospots-ISP-Server";

    String SRV_NAME_SCHEDULE_EXECUTOR = "SCHEDULE_EXECUTOR";
    String SRV_NAME_SCHEDULE_CONSOLE = "SCHEDULE_CONSOLE";

    String SRV_NAME_APP_VISITOR = "APP_VISITOR";


    static String serverAddress() {
        return System.getProperty("server.address");
    }

    static String webHookApi(String serverAddress) {
        return "http://" + serverAddress + PathConstant.ROOT_PATH + PathConstant.WORKFLOW_RUNTIME_PATH;
    }

    static String webHookApiTest(String serverAddress) {
        return "http://" + serverAddress + PathConstant.ROOT_PATH + PathConstant.WORKFLOW_TEST_PATH;
    }
}
