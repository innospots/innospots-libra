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

package io.innospots.workflow.core.execution.store;

import io.innospots.base.store.AsyncDataStore;
import io.innospots.workflow.core.execution.enums.RecordMode;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.operator.INodeExecutionOperator;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;

/**
 * @author Smars
 * @date 2021/3/12
 */
@Slf4j
public class NodeExecutionStoreListener implements INodeExecutionListener {

    private AsyncDataStore<NodeExecution> nodeExecutionAsyncDataStore;
    private INodeExecutionOperator nodeExecutionOperator;


    public NodeExecutionStoreListener(INodeExecutionOperator nodeExecutionOperator) {
        this.nodeExecutionOperator = nodeExecutionOperator;
        nodeExecutionAsyncDataStore = new AsyncDataStore<>(nodeExecutionOperator, 3, 100, "node-execution");
    }

    @Override
    public void start(NodeExecution nodeExecution) {
        log.debug("node execution start time:{} {}", LocalDateTime.now(), nodeExecution);
        nodeExecution.setStartTime(LocalDateTime.now());
    }

    @Override
    public void complete(NodeExecution nodeExecution) {
        log.debug("node execution complete time:{} {}", LocalDateTime.now(), nodeExecution);
        if (!nodeExecution.isSkipNodeExecution()) {
            if (nodeExecution.getRecordMode() == RecordMode.ASYNC) {
                nodeExecutionAsyncDataStore.insert(nodeExecution);
            } else {
                nodeExecutionOperator.insert(nodeExecution);
            }

        } else {
            log.debug("node execution complete not store time:{} {}", LocalDateTime.now(), nodeExecution);
        }
    }

    @Override
    public void fail(NodeExecution nodeExecution) {
        log.debug("node execution fail time:{} {}", LocalDateTime.now(), nodeExecution);
        if (!nodeExecution.isSkipNodeExecution()) {
            if (nodeExecution.getRecordMode() == RecordMode.SYNC) {
                nodeExecutionAsyncDataStore.insert(nodeExecution);
            } else {
                nodeExecutionOperator.insert(nodeExecution);
            }
        } else {
            log.info("node execution fail not store time:{} {}", LocalDateTime.now(), nodeExecution);
        }
    }


    @PreDestroy
    public void close() {
        if (nodeExecutionAsyncDataStore != null) {
            nodeExecutionAsyncDataStore.close();
        }
    }
}
