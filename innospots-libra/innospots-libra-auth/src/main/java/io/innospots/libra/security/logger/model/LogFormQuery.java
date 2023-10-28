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

package io.innospots.libra.security.logger.model;

import io.innospots.base.data.request.FormQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * logn log query request
 *
 * @author chenc
 * @date 2021/5/28 23:01
 */
@Getter
@Setter
@Schema(title = "login log query request")
public class LogFormQuery extends FormQuery {

    @Schema(title = "browser")
    private String browser;

    @Schema(title = "os")
    private String os;

    @Schema(title = "log status")
    private String status;

    @Schema(title = "user name")
    private List<String> usernames;

    @Schema(title = "user id")
    private List<Integer> userIds;

    @Schema(title = "query from time, include")
    private String fromTime;

    @Schema(title = "query end time, exclude")
    private String endTime;
}