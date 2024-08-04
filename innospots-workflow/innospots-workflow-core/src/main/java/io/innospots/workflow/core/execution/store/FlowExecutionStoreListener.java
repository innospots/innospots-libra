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
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.IFlowExecutionListener;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import jakarta.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;


/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class FlowExecutionStoreListener implements IFlowExecutionListener {

    private AsyncDataStore<FlowExecution> flowExecutionAsyncDataStore;
    private IFlowExecutionOperator flowExecutionOperator;

    public FlowExecutionStoreListener(IFlowExecutionOperator flowExecutionOperator) {
        this.flowExecutionOperator = flowExecutionOperator;
        flowExecutionAsyncDataStore = new AsyncDataStore<>(flowExecutionOperator, 4, 100, "event-store");
    }

    @Override
    public void start(FlowExecution flowExecution) {
        log.debug("runtime content store start time:{}", LocalDateTime.now());
        if (!flowExecution.isSkipFlowExecution()) {
            if(flowExecution.getRecordMode() == RecordMode.SYNC){
                flowExecutionOperator.insert(flowExecution);
            }else{
                flowExecutionAsyncDataStore.insert(flowExecution);
            }
        }
    }

    @Override
    public void complete(FlowExecution flowExecution) {
        log.debug("runtime content store end time:{}", LocalDateTime.now());
        if (!flowExecution.isSkipFlowExecution()) {
            this.update(flowExecution);
        }
    }

    @Override
    public void update(FlowExecution flowExecution) {
        if(flowExecution.getRecordMode() == RecordMode.SYNC){
            flowExecutionOperator.update(flowExecution);
        }else{
            flowExecutionAsyncDataStore.update(flowExecution);
        }

    }

    @PreDestroy
    public void close() {
        if (this.flowExecutionAsyncDataStore != null) {
            this.flowExecutionAsyncDataStore.close();
        }
    }

}
