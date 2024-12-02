/*
 *
 *  * Licensed to the Apache Software Foundation (ASF) under one
 *  * or more contributor license agreements.  See the NOTICE file
 *  * distributed with this work for additional information
 *  * regarding copyright ownership.  The ASF licenses this file
 *  * to you under the Apache License, Version 2.0 (the
 *  * "License"); you may not use this file except in compliance
 *  * with the License.  You may obtain a copy of the License at
 *  *
 *  *      http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing,
 *  * software distributed under the License is distributed on an
 *  * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  * KIND, either express or implied.  See the License for the
 *  * specific language governing permissions and limitations
 *  * under the License.
 *
 */

package io.innospots.libra.security.auth.model;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.PBaseEntity;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author castor_ling
 * @date 2024/11/27
 */
@Getter
@Setter
@Builder
public class OauthUser {
    /**
     * 系统用户ID
     */
    private Integer sysUserId;

    /**
     * OAuth提供商(如Google、Facebook等)
     */
    private String provider;

    /**
     * OAuth提供商返回的唯一用户ID
     */
    private String providerId;
    /**
     * OAuth提供商返回的唯一ID
     */
    private String providerUuid;

    /**
     * 用户邮箱
     */
    private String email;

    /**
     * 用户名
     */
    private String name;

    /**
     * 用户头像URL
     */
    private String pictureUrl;

    /**
     * 最后登录时间
     */
    private Long lastLoginTime;


}
