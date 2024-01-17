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

import io.innospots.base.enums.DataStatus;
import io.innospots.base.model.field.ParamField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/26
 */
@Setter
@Getter
public class NotificationChannel {

    private Integer channelId;

    @NotNull(message = "channel name must not be null")
    @Size(max = 16, message = "channel name is too long")
    private String channelName;

    @NotNull(message = "APP Type must not be null")
    private ChannelType channelType;

    private DataStatus status;

    private String registryId;

    private Integer credentialId;

    @NotEmpty(message = "params must not be null")
    private List<ParamField> params;


    /**
     * message channel type
     */
    public enum ChannelType {
        /**
         *
         */
        INBOX,
        EMAIL,
        APP;
    }
}
