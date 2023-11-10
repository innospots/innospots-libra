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

package io.innospots.workflow.console.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.libra.base.events.PageCreatedEvent;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @author Jegy
 * @version 1.0.0
 * @date 2022/5/23
 */
@Slf4j
@Component
public class WorkflowPageListener implements IEventListener<PageCreatedEvent> {

    public static final String PAGE_TYPE = "workflow";

    private final WorkflowInstanceOperator workflowInstanceOperator;

    public WorkflowPageListener(WorkflowInstanceOperator workflowInstanceOperator) {
        this.workflowInstanceOperator = workflowInstanceOperator;
    }


    @Override
    public Object listen(PageCreatedEvent pageCreatedEvent) {
        if (PAGE_TYPE.equals(pageCreatedEvent.getPageType())) {
            workflowInstanceOperator.bindPage(pageCreatedEvent.getPageId(), (Long) pageCreatedEvent.getBody());
        }
        return null;
    }
}