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
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/18
 */
@Getter
@Setter
public class MessageEvent extends EventBody {

    private String code;

    private String title;

    private String message;

    private String eventName;

    private String extName;

    private String module;

    public MessageEvent(Object source) {
        super(source);
    }

    public MessageEvent(String code, String title, String message, String eventName, String extName, String module) {
        super(code);
        this.code = code;
        this.title = title;
        this.message = message;
        this.eventName = eventName;
        this.extName = extName;
        this.module = module;
    }
}
