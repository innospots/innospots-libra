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

package io.innospots.workflow.schedule.flow;

import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.workflow.core.flow.WorkflowBody;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Set;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class FlowJobManager {

    private JobExecutionOperator jobExecutionOperator;

    private ReadJobDispatcher readJobDispatcher;

    private IWorkflowLoader workflowLoader;

    public void executeFlowJob(JobExecution jobExecution){
        //load workflow
        WorkflowBody workflowBody = workflowLoader.loadWorkflow(jobExecution.getKey());
        //select complete keys
        Set<String> completeKeys = completeJobKeys(jobExecution.getParentExecutionId());
        //select next can be executed nodes
        List<NodeInstance> instances = selectShouldExecuteNodes(workflowBody,completeKeys);
        if(CollectionUtils.isEmpty(instances)){
            return;
        }
        for (NodeInstance instance : instances) {
            readJobDispatcher.execute(readyJob(jobExecution,instance));
        }
    }

    private List<NodeInstance> selectShouldExecuteNodes(WorkflowBody workflowBody,Set<String> completeKeys){
        List<NodeInstance> nodeInstances = null;

        return nodeInstances;
    }

    private ReadyJob readyJob(JobExecution parentExecution,NodeInstance nodeInstance){

        return null;
    }

    /**
     *
     * @param parentExecutionId
     * @return
     */
    private Set<String> completeJobKeys(String parentExecutionId){
        return jobExecutionOperator.completeJobKeys(parentExecutionId);
    }

}
