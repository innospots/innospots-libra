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

package io.innospots.schedule.events;

import io.innospots.base.events.EventBody;
import io.innospots.schedule.enums.MessageStatus;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/27
 */
public class JobQueueEvent extends EventBody {

    private String jobReadyKey;

    private MessageStatus messageStatus;

    public JobQueueEvent(Object body) {
        super(body);
    }

    public void setJobReadyKey(String jobReadyKey) {
        this.jobReadyKey = jobReadyKey;
    }

    public MessageStatus getMessageStatus() {
        return messageStatus;
    }

    public void setMessageStatus(MessageStatus messageStatus) {
        this.messageStatus = messageStatus;
    }

    public String getParentExecutionId() {
        return (String) body;
    }

    public String getJobReadyKey() {
        return jobReadyKey;
    }
}
