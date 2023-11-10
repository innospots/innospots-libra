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

package io.innospots.libra.base.user;

import io.innospots.base.model.user.SimpleUser;
import io.innospots.base.model.user.UserInfo;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/30
 */
public interface SysUserReader {

    /**
     * get user by primary ids
     *
     * @param userIds
     * @return
     */
    List<UserInfo> listUsersByIds(List<Integer> userIds);

    /**
     * get user by names
     *
     * @param userNames
     * @return
     */
    List<UserInfo> listUsersByNames(List<String> userNames);

    List<UserInfo> listUserByRoleIds(List<Integer> roleIds);

    /**
     * get user by primary id
     *
     * @param userId
     * @return
     */
    UserInfo getUserInfo(Integer userId);

    /**
     * get user simple content
     *
     * @param userName
     * @return
     */
    SimpleUser getSimpleUser(String userName);
}
