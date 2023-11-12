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

package io.innospots.workflow.core.execution.model.node;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.enums.ExecMode;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.enums.RecordMode;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

/**
 * @author Raydian
 * @date 2020/12/20
 */
@Getter
@Setter
@Slf4j
public class NodeExecutionBase {

    protected String nodeKey;
    protected String flowExecutionId;
    protected String nodeExecutionId;
    protected Long flowInstanceId;
    protected Integer revision;
    protected ExecutionStatus status;
    protected LocalDateTime startTime;
    protected LocalDateTime endTime;
    protected boolean next;

    protected String message;

    protected Integer sequenceNumber;

    protected boolean skipNodeExecution;

    @JsonIgnore
    protected LocalDateTime flowStartTime;

    @JsonIgnore
    protected ExecMode execMode;

    protected String nodeName;

    protected String nodeCode;

    protected RecordMode recordMode=RecordMode.SYNC;


    public String flowKey() {
        if (this.nodeExecutionId == null) {
            return null;
        }
        int p = nodeExecutionId.indexOf("fk_");
        if (p < 0) {
            return null;
        }
        p += 3;
        int e = nodeExecutionId.indexOf("_", p);
        return nodeExecutionId.substring(p, e);
    }


    public void end(String msg, ExecutionStatus status, boolean next) {
        this.end(msg);
        this.status = status;
        this.next = next;
    }

    public void end(String msg) {
        endTime = LocalDateTime.now();
        if (StringUtils.isNotEmpty(msg)) {
            this.message = msg;
        }
    }

    public String consume() {
        if (this.endTime == null || this.startTime == null) {
            return "not complete.";
        }
        ZoneOffset offset = OffsetDateTime.now().getOffset();
        return DateTimeUtils.consume(
                this.endTime.toInstant(offset).toEpochMilli(),
                this.startTime.toInstant(offset).toEpochMilli());
    }

    public String getConsume(){
        return consume();
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("nodeKey='").append(nodeKey).append('\'');
        sb.append("nodeExecutionId='").append(nodeExecutionId).append('\'');
        sb.append(", flowInstanceId=").append(flowInstanceId);
        sb.append(", revision=").append(revision);
        sb.append(", status=").append(status);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append('}');
        return sb.toString();
    }
}
