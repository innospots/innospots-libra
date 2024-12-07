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

package io.innospots.libra.kernel.module.system.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.kernel.module.system.entity.SysUserEntity;
import io.innospots.libra.kernel.module.system.entity.SysUserGroupEntity;
import io.innospots.libra.kernel.module.system.model.user.UserForm;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author chenc
 * @date 2021/12/25
 */
@Mapper
public interface UserInfoConverter extends BaseBeanConverter<UserInfo,SysUserEntity> {

    UserInfoConverter INSTANCE = Mappers.getMapper(UserInfoConverter.class);

    SysUserEntity formModel2Entity(UserForm userInfo);

    UserInfo simple2Info(SimpleUser simpleUser);

    default List<SimpleUser> entitiesToSimpleUsers(List<SysUserEntity> entities, Map<Integer, String> groupEntityMap){
        List<SimpleUser> simpleUsers = new ArrayList<>();
        for (SysUserEntity entity : entities) {
            SimpleUser simpleUser = simple2Info(entityToModel(entity));
            simpleUser.setGroupName(groupEntityMap.get(entity.getGroupId()));
            simpleUsers.add(simpleUser);
        }
        return simpleUsers;
    }
}
