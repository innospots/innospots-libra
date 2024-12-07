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

package io.innospots.libra.security.auth.entity;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.entity.BaseEntity;
import io.innospots.base.entity.PBaseEntity;
import jakarta.persistence.*;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2024/11/25
 */
@Getter
@Setter
@Entity
@TableName(value = OauthUserEntity.TABLE_NAME)
@Table(name = OauthUserEntity.TABLE_NAME)
public class OauthUserEntity extends BaseEntity {

    public static final String TABLE_NAME = "sys_oauth_user";

    /**
     * 主键
     */
    @Id
    @Column
    @TableId(type = IdType.AUTO)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer oauthUserId;

    /**
     * 系统用户ID
     */
    @Column
    private Integer sysUserId;

    /**
     * OAuth提供商(如Google、Facebook等)
     */
    @Column(length = 32)
    private String provider;

    /**
     * OAuth提供商返回的唯一用户ID
     */
    @Column
    private String providerId;

    /**
     * OAuth提供商返回的ID
     */
    @Column
    private String providerUuid;

    /**
     * 用户邮箱
     */
    @Column(length = 128)
    private String email;

    /**
     * 用户名
     */
    @Column(length = 128)
    private String name;

    /**
     * 用户头像URL
     */
    @Column
    private String pictureUrl;

    /**
     * 最后登录时间
     */
    @Column
    private LocalDateTime lastLoginTime;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("OauthUserEntity{");
        sb.append("createdTime=").append(createdTime);
        sb.append(", updatedTime=").append(updatedTime);
        sb.append(", oauthUserId=").append(oauthUserId);
        sb.append(", sysUserId=").append(sysUserId);
        sb.append(", oauthUserId=").append(oauthUserId);
        sb.append(", providerUuid='").append(providerUuid).append('\'');
        sb.append(", email='").append(email).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", pictureUrl='").append(pictureUrl).append('\'');
        sb.append(", provider=").append(provider).append('\'');
        sb.append('}');
        return sb.toString();
    }


}
