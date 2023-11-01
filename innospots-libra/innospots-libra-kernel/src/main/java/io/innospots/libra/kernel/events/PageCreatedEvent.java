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

package io.innospots.libra.kernel.events;

import io.innospots.base.events.EventBody;
import org.springframework.context.ApplicationEvent;

/**
 * @author Jegy
 * @version 1.0.0
 * @date 2022/5/23
 */
public class PageCreatedEvent extends EventBody {

    private Integer pageId;

    protected String pageType;

    public PageCreatedEvent(Object source) {
        super(source);
    }

    public PageCreatedEvent(Object source, Integer pageId, String pageType) {
        super(source);
        this.pageId = pageId;
        this.pageType = pageType;
    }

    public Integer getPageId() {
        return pageId;
    }

    public String getPageType() {
        return pageType;
    }
}