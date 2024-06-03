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

import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.schedule.dispatch.ReadJobDispatcher;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.job.BaseJob;
import io.innospots.schedule.model.JobExecution;
import io.innospots.schedule.model.ReadyJob;
import io.innospots.schedule.operator.JobExecutionOperator;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.flow.loader.IWorkflowLoader;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.schedule.job.FlowNodeJob;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/10
 */
public class FlowJobExecutor {

    private JobExecutionOperator jobExecutionOperator;

    private ReadJobDispatcher readJobDispatcher;

    private IWorkflowLoader workflowLoader;


    public FlowJobExecutor(JobExecutionOperator jobExecutionOperator,
                           ReadJobDispatcher readJobDispatcher,
                           IWorkflowLoader workflowLoader) {
        this.jobExecutionOperator = jobExecutionOperator;
        this.readJobDispatcher = readJobDispatcher;
        this.workflowLoader = workflowLoader;
    }

    public WorkflowBody readJobFlow(String flowKey) {
        return workflowLoader.loadWorkflow(flowKey);
    }

    /**
     *
     * @param jobExecution
     * @param workflowBody
     */
    public void executeStartJob(JobExecution jobExecution,WorkflowBody workflowBody) {
        Map p = jobExecution.getMap(BaseJob.PARAM_EXECUTE_JOB_PARAMS);
        List<NodeInstance> startNodes = workflowBody.getStarts();
        List<Map<String,Object>> paramList = new ArrayList<>();
        if(MapUtils.isNotEmpty(p)){
            paramList.add(p);
        }
        for (NodeInstance startNode : startNodes) {
            ReadyJob readyJob = buildReadyJob(jobExecution, startNode,paramList, 1);
            readJobDispatcher.dispatch(readyJob);
        }
    }

    public void executeNodeJob(JobExecution jobExecution) {
        //load workflow
        WorkflowBody workflowBody = workflowLoader.loadWorkflow(jobExecution.getJobKey());
        //select execute map keys
        List<NodeJobExecution> nodeJobExecutions = nodeJobExecutions(jobExecution, workflowBody);

        //select next can be executed nodes
        Collection<NodeJobExecution> nodeShouldExecutions = selectShouldExecuteNodes(jobExecution, workflowBody, nodeJobExecutions);

        if (CollectionUtils.isEmpty(nodeShouldExecutions)) {
            return;
        }
        int sequenceNumber = nodeJobExecutions.size();
        for (NodeJobExecution nje : nodeShouldExecutions) {
            sequenceNumber += 1;
            readJobDispatcher.dispatch(buildReadyJob(jobExecution, nje.ni,nje.prevOutputs, sequenceNumber));
        }
    }

    private Collection<NodeJobExecution> selectShouldExecuteNodes(JobExecution jobExecution, WorkflowBody workflowBody, List<NodeJobExecution> nodeJobExecutions) {
        List<String> nonExecuteKeys = readJobDispatcher.readNonExecuteJobKeys(jobExecution.getExecutionId());
        Set<String> readyKeys = new HashSet<>();
        if (CollectionUtils.isNotEmpty(nonExecuteKeys)) {
            readyKeys.addAll(nonExecuteKeys);
        }
        Map<String, NodeJobExecution> shouldExecutionJobs = new HashMap<>();
        Set<String> completeKeys = new HashSet<>();
        Set<String> failKeys = new HashSet<>();
        Map<String, NodeJobExecution> nodeExtMap = new HashMap<>();
        for (NodeJobExecution nodeJobExecution : nodeJobExecutions) {
            String nodeKey = nodeJobExecution.ni.getNodeKey();
            if (nodeJobExecution.executionStatus() == ExecutionStatus.COMPLETE) {
                completeKeys.add(nodeKey);
            } else if (nodeJobExecution.executionStatus() == ExecutionStatus.FAILED) {
                failKeys.add(nodeKey);
            }
            nodeExtMap.put(nodeKey, nodeJobExecution);
        }

        for (String completeKey : completeKeys) {
            fillShouldExecuteNode(workflowBody, completeKey, readyKeys, nodeExtMap, completeKeys, shouldExecutionJobs);
        }//complete key

        for (String failKey : failKeys) {
            NodeInstance failNode = workflowBody.findNode(failKey);
            if (!failNode.isContinueOnFail()) {
                //skip fail node
                continue;
            }
            fillShouldExecuteNode(workflowBody, failKey, readyKeys, nodeExtMap, completeKeys, shouldExecutionJobs);
        }//fail key but can be to continue execute

        return shouldExecutionJobs.values();
    }

    private void fillShouldExecuteNode(WorkflowBody workflowBody, String currentNodeKey,
                                       Set<String> readyKeys,
                                       Map<String, NodeJobExecution> nodeExtMap,
                                       Set<String> completeKeys, Map<String, NodeJobExecution> shouldNodeExecutionMap) {
        NodeInstance currentNode = workflowBody.findNode(currentNodeKey);
        if (CollectionUtils.isEmpty(currentNode.getNextNodeKeys())) {
            return;
        }
        //next node
        for (String nextNodeKey : currentNode.getNextNodeKeys()) {
            if (nodeExtMap.containsKey(nextNodeKey) ||
                    readyKeys.contains(nextNodeKey) ||
                    shouldNodeExecutionMap.containsKey(nextNodeKey)) {
                //has executed
                continue;
            }
            NodeInstance nextNode = workflowBody.findNode(nextNodeKey);
            Set<String> prevNotExecKeys = new HashSet<>(nextNode.getPrevNodeKeys());
            prevNotExecKeys.removeAll(completeKeys);
            if (!prevNotExecKeys.isEmpty()) {
                for (String prevKey : prevNotExecKeys) {
                    NodeJobExecution nje = nodeExtMap.get(prevKey);
                    if (nje == null) {
                        return;
                    }
                    if (nje.je.getExecutionStatus() == ExecutionStatus.FAILED && nje.ni.isContinueOnFail()) {
                        prevNotExecKeys.remove(prevKey);
                    }
                }
            }

            if (prevNotExecKeys.isEmpty()) {
                //All dependent node jobs have been executed
                NodeJobExecution nje = new NodeJobExecution();
                nje.ni = nextNode;
                //fill prev node outputs
                for (String prevNodeKey : nextNode.getPrevNodeKeys()) {
                    NodeJobExecution prevNodeJobExecution = nodeExtMap.get(prevNodeKey);
                    if (MapUtils.isNotEmpty(prevNodeJobExecution.je.getOutput())) {
                        nje.prevOutputs.add(prevNodeJobExecution.je.getOutput());
                    }
                }//end for preNodeKey
                shouldNodeExecutionMap.put(nje.ni.getNodeKey(), nje);
            }
        }//end for
    }


    private ReadyJob buildReadyJob(JobExecution jobExecution,NodeInstance ni, List<Map<String,Object>> inputParams, int count) {
        ReadyJob readyJob = new ReadyJob();
        readyJob.setJobKey(ni.getNodeKey());
        readyJob.setJobName(ni.getName());
        readyJob.setKeyType("node");
        readyJob.setJobClass(FlowNodeJob.class.getName());
        readyJob.setJobType(JobType.EXECUTE);
        readyJob.setScopes(jobExecution.getScopes());
        readyJob.setParentExecutionId(jobExecution.getExecutionId());
        readyJob.setExtExecutionId(jobExecution.getExtExecutionId());
        readyJob.setMessageStatus(MessageStatus.UNREAD);
        readyJob.setSequenceNumber(count);
        //flowKey
        readyJob.setResourceKey(jobExecution.getJobKey());
        Map<String, Object> context = new HashMap<>();
        for (Map<String, Object> prevOutput : inputParams) {
            context.putAll(prevOutput);
        }
        Map eMap = jobExecution.getMap(BaseJob.PARAM_EXECUTE_JOB_PARAMS);
        if (MapUtils.isNotEmpty(eMap)) {
            context.putAll(eMap);
        }
        readyJob.setContext(context);

        return readyJob;
    }

    /**
     * @param parentExecution
     * @return
     */
    private List<NodeJobExecution> nodeJobExecutions(JobExecution parentExecution, WorkflowBody workflowBody) {
        List<JobExecution> subJobExecutions = jobExecutionOperator.getSubJobExecutions(parentExecution.getExecutionId());
        if (CollectionUtils.isEmpty(subJobExecutions)) {
            return Collections.emptyList();
        }

        Map<String, JobExecution> jobKeyMap = subJobExecutions.stream()
                .collect(Collectors.toMap(JobExecution::getJobKey, jobExecution -> jobExecution));

        List<NodeJobExecution> nodeJobExecutions = new ArrayList<>();
        for (JobExecution jobExecution : subJobExecutions) {
            NodeJobExecution nodeJobExecution = new NodeJobExecution();
            nodeJobExecution.je = jobExecution;
            NodeInstance ni = workflowBody.findNode(jobExecution.getJobKey());
            nodeJobExecution.ni = ni;
            if (CollectionUtils.isNotEmpty(ni.getPrevNodeKeys())) {
                for (String prevNodeKey : ni.getPrevNodeKeys()) {
                    JobExecution prevJobExecution = jobKeyMap.get(prevNodeKey);
                    if (prevJobExecution != null && MapUtils.isNotEmpty(prevJobExecution.getOutput())) {
                        nodeJobExecution.prevOutputs.add(prevJobExecution.getOutput());
                    }
                }//end for
            }

            nodeJobExecutions.add(nodeJobExecution);
        }
        return nodeJobExecutions;
    }

    private class NodeJobExecution {
        JobExecution je;
        NodeInstance ni;
        List<Map<String, Object>> prevOutputs = new ArrayList<>();

        public ExecutionStatus executionStatus() {
            return je.getExecutionStatus();
        }
    }

}
