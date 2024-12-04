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

package io.innospots.libra.security.operator;

import cn.hutool.core.date.DateTime;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.events.NotificationAnnotation;
import io.innospots.libra.base.user.AuthSysUserReader;
import io.innospots.libra.security.auth.basic.OAuthUserDao;
import io.innospots.libra.security.auth.entity.OauthUserEntity;
import io.innospots.libra.security.auth.model.OauthUser;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * @author castor_ling
 * @date 2024/11/27
 */
@Service
public class OauthUserOperator extends ServiceImpl<OAuthUserDao, OauthUserEntity> {
    @Autowired
    private AuthSysUserReader authSysUserReader;

    private static Integer ROLE_ID = 1;

    /**
     * get by providerId and provider
     *
     * @param providerId,provider
     * @return
     */
    public OauthUserEntity getByProviderId(String providerId, String provider) {
        if (StringUtils.isBlank(providerId) || StringUtils.isBlank(provider)) {
            return null;
        }
        QueryWrapper<OauthUserEntity> query = new QueryWrapper<>();
        query.lambda().eq(OauthUserEntity::getProvider, provider)
                .eq(OauthUserEntity::getProviderId,providerId);
        query.last("limit 1");
        OauthUserEntity oauthUser = super.getOne(query);
        return oauthUser;
    }

    @Transactional(rollbackFor = Exception.class)
    @NotificationAnnotation(name = "${event.create.oauthuser}", code = "create_oauth_user",
            module = "libra-auth",
            title = "${event.create.oauthuser.title}", content = "${event.create.oauthuser.content}")
    public UserInfo createOauthUser(OauthUser oauthUser) {
        UserInfo userInfo = authSysUserReader.creatOauthUser(oauthUser2UserInfo(oauthUser));
        OauthUserEntity oauthUserEntity =  OauthUserEntity.builder()
                .sysUserId(userInfo.getUserId())
                .email(oauthUser.getEmail())
                .provider(oauthUser.getProvider())
                .providerId(oauthUser.getProviderId())
                .providerUuid(oauthUser.getProviderUuid())
                .name(oauthUser.getName())
                .pictureUrl(oauthUser.getPictureUrl())
                .lastLoginTime(new DateTime())
                .build();
         this.save(oauthUserEntity);
         return userInfo;
    }
    private UserInfo oauthUser2UserInfo (OauthUser oauthUser){
        UserInfo userInfo = new UserInfo();
        userInfo.setAvatarKey(oauthUser.getPictureUrl());
        userInfo.setRealName( oauthUser.getName() );
        userInfo.setUserName( oauthUser.getProviderId() );
        userInfo.setLastAccessTime( LocalDateTime.now() );
        userInfo.setEmail( oauthUser.getEmail() );
        userInfo.setPassword( oauthUser.getEmail() );
        userInfo.setRoleIds(new ArrayList<>(List.of(ROLE_ID)));
        return userInfo;
    }
}