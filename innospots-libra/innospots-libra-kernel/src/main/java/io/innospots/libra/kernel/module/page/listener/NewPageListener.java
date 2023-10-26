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

package io.innospots.libra.kernel.module.page.listener;

import io.innospots.base.utils.BeanContextAware;
import io.innospots.libra.base.event.NewPageEvent;
import io.innospots.libra.base.event.PageCreatedEvent;
import io.innospots.libra.kernel.module.page.enums.PageOperationType;
import io.innospots.libra.kernel.module.page.model.PageDetail;
import io.innospots.libra.kernel.module.page.operator.PageOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

/**
 * @author Jegy
 * @version 1.0.0
 * @date 2022/5/23
 */
@Slf4j
@Component
public class NewPageListener {

    private final PageOperator pageOperator;

    public NewPageListener(PageOperator pageOperator) {
        this.pageOperator = pageOperator;
    }

    @EventListener(value = NewPageEvent.class)
    public void handleEvent(NewPageEvent newPageEvent) {
        PageDetail pageDetail = new PageDetail();
        pageDetail.setPageType(newPageEvent.getPageType());
        pageDetail = pageOperator.createOrUpdate(pageDetail, PageOperationType.SAVE);
        if (pageDetail != null) {
            BeanContextAware.sendAppEvent(new PageCreatedEvent(newPageEvent.getSource(), pageDetail.getId(), newPageEvent.getPageType()));
        }
    }
}