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

package io.innospots.libra.kernel.module.notification.model;

import io.innospots.base.json.annotation.I18n;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import jakarta.validation.constraints.NotNull;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Getter
@Setter
@Schema(title = "setting channel ,group and event")
public class NotificationSetting {

    private Integer settingId;

    @NotNull
    private String moduleKey;
    private String moduleName;

    private String extName;

    private String extKey;

    @I18n
    private String eventName;

    @NotNull
    private String eventCode;

    private List<Integer> channels;

    private List<Integer> groups;
}
