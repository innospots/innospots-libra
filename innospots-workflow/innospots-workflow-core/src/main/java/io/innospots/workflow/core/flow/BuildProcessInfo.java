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

package io.innospots.workflow.core.flow;


import io.innospots.workflow.core.enums.FlowStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.LongAdder;

/**
 * @author Smars
 * @date 2022/2/15
 */
public class BuildProcessInfo {

    private Long workflowInstanceId;

    private String flowKey;

    private String datasourceCode;

    private String message;

    private FlowStatus status;

    private Long startTime;

    private Long endTime;

    private String detail;

    private LinkedHashMap<String, Exception> errorInfo = new LinkedHashMap<>();

    private LongAdder successCounter = new LongAdder();

    private LongAdder failCounter = new LongAdder();

    private Exception buildException;

    private String triggerCode;

    public boolean isLoaded() {
        return FlowStatus.LOADED.equals(status) && buildException == null && failCounter.intValue() == 0;
    }

    public Exception getBuildException() {
        return buildException;
    }

    public void setBuildException(Exception buildException) {
        this.buildException = buildException;
    }

    public void addNodeProcess(String nodeKey, Exception exception) {
        this.errorInfo.put(nodeKey, exception);
    }

    public String getBuildMessage(String nodeKey) {
        return this.errorInfo != null && this.errorInfo.containsKey(nodeKey) ? this.errorInfo.get(nodeKey).toString() : "build success";
    }

    public LinkedHashMap<String, Exception> getErrorInfo() {
        return errorInfo;
    }

    public String errorMessage() {
        return this.buildException != null ? this.buildException.getClass() + " , " + this.buildException.getMessage() : "";
    }

    public void incrementSuccess() {
        successCounter.increment();
    }

    public void incrementFail() {
        failCounter.increment();
    }

    public int getFailCount() {
        return failCounter.intValue();
    }

    public int getSuccessCount() {
        return successCounter.intValue();
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public FlowStatus getStatus() {
        return status;
    }

    public void setStatus(FlowStatus status) {
        this.status = status;
    }

    public Long getWorkflowInstanceId() {
        return workflowInstanceId;
    }

    public void setWorkflowInstanceId(Long workflowInstanceId) {
        this.workflowInstanceId = workflowInstanceId;
    }

    public Long getStartTime() {
        return startTime;
    }

    public void setStartTime(Long startTime) {
        this.startTime = startTime;
    }

    public Long getEndTime() {
        return endTime;
    }

    public void setEndTime(Long endTime) {
        this.endTime = endTime;
    }

    public String getDetail() {
        return detail;
    }

    public void setDetail(String detail) {
        this.detail = detail;
    }

    public String getFlowKey() {
        return flowKey;
    }

    public void setFlowKey(String flowKey) {
        this.flowKey = flowKey;
    }

    public String getDatasourceCode() {
        return datasourceCode;
    }

    public String getTriggerCode() {
        return triggerCode;
    }

    public void setTriggerCode(String triggerCode) {
        this.triggerCode = triggerCode;
    }

    public void setDatasourceCode(String datasourceCode) {
        this.datasourceCode = datasourceCode;
    }

    public String detail() {
        StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, Exception> entry : errorInfo.entrySet()) {
            buf.append(entry.getKey());
            buf.append("=");
            buf.append(entry.getValue().toString());
            buf.append("\n");
        }
        return buf.toString();
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("workflowInstanceId=").append(workflowInstanceId);
        sb.append(", message='").append(message).append('\'');
        sb.append(", status=").append(status);
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", errorInfo=").append(errorInfo);
        sb.append(", successCounter=").append(successCounter);
        sb.append(", failCounter=").append(failCounter);
        sb.append(", detail='").append(detail).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
