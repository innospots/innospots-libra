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

package io.innospots.libra.kernel.module.system.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.libra.kernel.module.system.model.role.RoleDelEvent;
import io.innospots.libra.kernel.module.system.operator.RoleResourceOperator;
import io.innospots.libra.kernel.module.system.operator.UserRoleOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.util.Collections;

/**
 * RoleDelListener
 *
 * @author chenc
 * @date 2022/12/05
 */
@Slf4j
@Component
public class RoleDelListener implements IEventListener<RoleDelEvent> {

    private final UserRoleOperator userRoleOperator;

    private final RoleResourceOperator roleResourceOperator;

    public RoleDelListener(UserRoleOperator userRoleOperator, RoleResourceOperator roleResourceOperator) {
        this.userRoleOperator = userRoleOperator;
        this.roleResourceOperator = roleResourceOperator;
    }


    @Override
    public Object listen(RoleDelEvent event) {
        Object source = event.getBody();
        if (source instanceof Integer) {
            Integer roleId = (Integer) source;
            userRoleOperator.deleteByRoleIds(Collections.singletonList(roleId));
            roleResourceOperator.deleteRoleResourceByRoleId(roleId);
        }
        return event.getBody();
    }
}