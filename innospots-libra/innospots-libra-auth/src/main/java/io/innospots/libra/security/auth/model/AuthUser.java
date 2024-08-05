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

package io.innospots.libra.security.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.enums.DataStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2021/2/16
 */
@Getter
@Setter
@Builder
@TableName(value = AuthUser.TABLE_NAME)
public class AuthUser {

    public static final String TABLE_NAME = "sys_user";

    @TableId(type = IdType.AUTO)
    private Integer userId;

    private String userName;

    private String realName;

    private String password;

    private Integer lastOrgId;

    private Integer lastProjectId;

    private DataStatus status;

}