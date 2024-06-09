/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.libra.base.events;

import io.innospots.base.enums.DataStatus;
import io.innospots.base.events.EventBody;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * crud some data, when the operation cross the module
 *
 * @author Smars
 * @date 2022/2/10
 */
@Getter
@Setter
public class ResourceActionEvent extends EventBody {

    private Object primaryId;

    private String module;

    private ResourceAction resourceAction;

    private DataStatus status;

    public ResourceActionEvent(String module, ResourceAction resourceAction,Object source) {
        super(source);
        this.module = module;
        this.resourceAction = resourceAction;
    }

    public enum ResourceAction {
        /**
         *
         */
        CREATE,
        //create or update
        SAVE,
        UPDATE,
        STATUS,
        DELETE
    }
}
