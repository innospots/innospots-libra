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
import io.innospots.libra.base.extension.ExtensionStatus;
import lombok.Getter;
import lombok.Setter;

/**
 * ApplicationEvent
 *
 * @author Wren
 * @date 2022/5/10-21:26
 */
@Getter
@Setter
public class ExtensionEvent extends EventBody {

    private String extensionKey;
    private String extensionName;
    private ExtensionStatus status;

    public ExtensionEvent(String extensionKey, String extensionName, ExtensionStatus status) {
        super();
        this.extensionKey = extensionKey;
        this.extensionName = extensionName;
        this.status = status;
    }

    public ExtensionEvent(Object source) {
        super(source);
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("AppEvent{");
        sb.append("appKey='").append(extensionKey).append('\'');
        sb.append(", appName='").append(extensionName).append('\'');
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }
}
