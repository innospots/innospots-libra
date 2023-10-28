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

package io.innospots.workflow.core.execution.flow;

import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.base.utils.Initializer;
import io.innospots.base.utils.InnospotIdGenerator;
import io.innospots.workflow.core.execution.ExecutionStatus;
import io.innospots.workflow.core.execution.node.NodeExecution;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author Raydian
 * @date 2020/12/20
 */
@Getter
@Setter
public class FlowExecutionBase implements Initializer {


    public static final String PROP_UUID = "uuid";
    public static final String PROP_UUID_TYPE = "uuidType";
    public static final String PROP_CHANNEL = "channel";
    public static final String PROP_LOCATION = "location";
    public static final String PROP_URI = "uri";
    public static final String PROP_SOURCE = "source";

    protected String flowExecutionId;

    protected Long flowInstanceId;

    protected Integer revision;

    protected String flowKey;

    /**
     * node execution store in memory
     */
    protected boolean memoryMode = true;

    /**
     * not store node execution
     */
    protected boolean skipNodeExecution;

    /**
     * not store flow execution
     */
    protected boolean skipFlowExecution;

    protected LocalDateTime startTime;

    protected LocalDateTime endTime;

    protected ExecutionStatus status;

    protected LocalDateTime updatedTime;

    protected LocalDateTime createdTime;

    protected String message;

    protected String result;

    protected String parentFlowExecutionId;


    protected String uuid;

    protected String uuidType;

    protected String channel;

    protected String location;

    protected String executionUri;

    /**
     * the trigger code
     */
    protected String source;

    protected String resultCode;

    protected boolean saveSync;


    @Setter(AccessLevel.NONE)
    private LinkedHashMap<String, NodeExecution> nodeExecutions = new LinkedHashMap<>();


    public FlowExecutionBase() {
        this.startTime = LocalDateTime.now();
    }


    @Override
    public void initialize() {
        if (flowExecutionId != null) {
            int s = flowExecutionId.indexOf("_");
            this.flowKey = this.flowExecutionId.substring(0, s);
        }
    }


    public void fillExecutionId(String flowKey) {
        this.flowKey = flowKey;
        this.setFlowExecutionId(String.join("_", flowKey, String.valueOf(InnospotIdGenerator.generateId())));
    }


    public void addNodeExecution(NodeExecution nodeExecution) {
        if (this.isSkipNodeExecution()) {
            nodeExecutions.put(nodeExecution.getNodeKey(), null);
        } else {
            nodeExecutions.put(nodeExecution.getNodeKey(), nodeExecution);
        }
    }

    public void addNodeExecutions(List<NodeExecution> nodeExecutions) {
        for (NodeExecution nodeExecution : nodeExecutions) {
            addNodeExecution(nodeExecution);
        }
    }

    public NodeExecution getNodeExecution(String nodeKey) {
        return nodeExecutions.get(nodeKey);
    }


    public boolean isDone(String nodeKey) {
        NodeExecution execution = getNodeExecution(nodeKey);
        return execution != null && execution.getStatus() != null && execution.getStatus().isDone();
    }

    public boolean isNotExecute(String nodeKey) {
        return !this.nodeExecutions.containsKey(nodeKey);
    }

    public boolean isExecuted(String nodeKey) {
        return this.nodeExecutions.containsKey(nodeKey);
    }

    public boolean isExecuting(String nodeKey) {
        NodeExecution execution = getNodeExecution(nodeKey);
        return execution != null && execution.getStatus() != null && execution.getStatus().isExecuting();
    }

    public boolean shouldStopped() {
        return this.status != null && (this.status.isStopping() || this.status.isDone());
    }


    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("executionId='").append(flowExecutionId).append('\'');
        sb.append(", startTime=").append(startTime);
        sb.append(", endTime=").append(endTime);
        sb.append(", status=").append(status);
        sb.append('}');
        return sb.toString();
    }

    public String getConsume(){
        LocalDateTime end = this.endTime !=null ? this.endTime : LocalDateTime.now();
        long e = end.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        LocalDateTime start = this.startTime !=null ? this.startTime : LocalDateTime.now();
        long s = start.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
        return DateTimeUtils.consume(e,s);
    }

}
