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

package io.innospots.workflow.console.service;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.base.data.request.FormQuery;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.console.model.flow.WorkflowChart;
import io.innospots.workflow.console.model.flow.WorkflowStatistics;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.innospots.workflow.console.operator.instance.WorkflowInstanceOperator;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import io.innospots.workflow.core.instance.model.WorkflowInstance;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Jegy
 * @version 1.0.0
 * @date 2022/3/27
 */
@Slf4j
@Service
public class WorkflowService {


    private final WorkflowInstanceOperator workflowInstanceOperator;

    private final IFlowExecutionOperator IFlowExecutionOperator;

    private final FlowNodeDefinitionOperator flowNodeDefinitionOperator;

    public WorkflowService(WorkflowInstanceOperator workflowInstanceOperator,
                           IFlowExecutionOperator IFlowExecutionOperator,
                           FlowNodeDefinitionOperator flowNodeDefinitionOperator) {
        this.workflowInstanceOperator = workflowInstanceOperator;
        this.IFlowExecutionOperator = IFlowExecutionOperator;
        this.flowNodeDefinitionOperator = flowNodeDefinitionOperator;
    }

    public PageBody<WorkflowInstance> getWorkflows(FormQuery request) {
        PageBody<WorkflowInstance> results = workflowInstanceOperator.pageWorkflows(request);
        List<WorkflowInstance> workflowInstances = results.getList();
        if (CollectionUtils.isNotEmpty(workflowInstances)) {
            List<String> triggerCodes = workflowInstances.stream().map(WorkflowInstance::getTriggerCode).distinct().collect(Collectors.toList());
            List<FlowNodeDefinitionEntity> appNodeDefinitionEntities = flowNodeDefinitionOperator.listByCodes(triggerCodes);
            Map<String, FlowNodeDefinitionEntity> appNodeDefinitionEntityMap = appNodeDefinitionEntities.stream()
                    .collect(Collectors.toMap(FlowNodeDefinitionEntity::getCode, Function.identity()));
            for (WorkflowInstance workflowInstance : workflowInstances) {
                workflowInstance.setIcon(appNodeDefinitionEntityMap.get(workflowInstance.getTriggerCode()).getIcon());
            }
        }
        return results;
    }

    public WorkflowStatistics getWorkflowStat(Long workflowInstanceId) {
        WorkflowInstance instance = workflowInstanceOperator.getWorkflowInstance(workflowInstanceId);
        String end = DateTimeUtils.formatDate(new Date(), "yyyyMMdd") + "999999.999";
        String start = DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -30), "yyyyMMdd") + "000000.000";
        PageBody<FlowExecutionBase> thirtyDaysBodies = IFlowExecutionOperator.pageFlowExecutions(workflowInstanceId, instance.getRevision(), start, end, null, null);
        List<FlowExecutionBase> thirtyDaysFlowExecutions = thirtyDaysBodies.getList();
        WorkflowStatistics workflowStatistics = new WorkflowStatistics();
        String today = DateTimeUtils.formatDate(new Date(), "yyyyMMdd");
        String yesterday = DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -1), "yyyyMMdd");
        Map<String, WorkflowChart> strategyChartMap = new HashMap<>();

        strategyChartMap = this.generateEveryDayFlowExecutionMap(thirtyDaysFlowExecutions);
        int todayWebhookCount = strategyChartMap.get(today).getCount();
        int yesterdayWebhookCount = strategyChartMap.get(yesterday).getCount();
        workflowStatistics.setTodayTimes(todayWebhookCount);
        BigDecimal rate = BigDecimal.ZERO;
        if (todayWebhookCount > 0 && yesterdayWebhookCount > 0) {
            rate = new BigDecimal((todayWebhookCount - yesterdayWebhookCount) + "").divide(new BigDecimal(yesterdayWebhookCount), 2, RoundingMode.HALF_DOWN).multiply(new BigDecimal("100"));
        }
        workflowStatistics.setGrowthRate(rate);
        PageBody<FlowExecutionBase> allBodies = IFlowExecutionOperator.pageFlowExecutions(workflowInstanceId, instance.getRevision(), null, null, null, null);
        List<FlowExecutionBase> allFlowExecutions = allBodies.getList();
        workflowStatistics.setCumulativeTimes(allFlowExecutions.size());

        /*
        switch (Objects.requireNonNull(WorkflowType.getWorkflowType(instance.getTemplateCode()))) {
            case SCHEDULE:
                strategyChartMap = this.generateEveryDaySuccessFlowExecutionMap(thirtyDaysFlowExecutions);
                WorkflowChart chart = strategyChartMap.get(today);
                workflowStatistics.setSuccessJob(chart.getSuccessCount());
                workflowStatistics.setFailJob(chart.getFailCount());
                break;
            case WEBHOOK:
            case STREAM:

                break;
            default:
                break;
        }

         */
        if (MapUtils.isNotEmpty(strategyChartMap)) {
            workflowStatistics.setCharts(IntStream.range(0, 30)
                    .mapToObj(i -> DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -i), "yyyyMMdd"))
                    .map(strategyChartMap::get).collect(Collectors.toCollection(() -> new ArrayList<>(30))));
        }
        return workflowStatistics;
    }


    /**
     * generate execute every day success and fail
     *
     * @param thirtyDaysFlowExecutions
     * @return
     */
    private Map<String, WorkflowChart> generateEveryDaySuccessFlowExecutionMap(List<FlowExecutionBase> thirtyDaysFlowExecutions) {
        Map<String, WorkflowChart> flowExecutionMap = new HashMap<>(30);
        if (CollectionUtils.isNotEmpty(thirtyDaysFlowExecutions)) {
            for (FlowExecutionBase flowExecution : thirtyDaysFlowExecutions) {
                String key = DateTimeUtils.formatLocalDateTime(flowExecution.getStartTime(), DateTimeUtils.DATETIME_DATA_PATTERN);
                WorkflowChart chart;
                if (MapUtils.isEmpty(flowExecutionMap) || flowExecutionMap.get(key) == null) {
                    chart = new WorkflowChart();
                    chart.setTime(key);
                    if (ExecutionStatus.COMPLETE == flowExecution.getStatus()) {
                        chart.setSuccessCount(1);

                    } else if (ExecutionStatus.FAILED == flowExecution.getStatus()) {
                        chart.setFailCount(1);
                    }

                } else {
                    chart = flowExecutionMap.get(key);
                    if (ExecutionStatus.COMPLETE == flowExecution.getStatus()) {
                        chart.setSuccessCount(chart.getSuccessCount() + 1);

                    } else if (ExecutionStatus.FAILED == flowExecution.getStatus()) {
                        chart.setFailCount(chart.getFailCount() + 1);
                    }
                }
                flowExecutionMap.put(key, chart);
            }
        }
        if (flowExecutionMap.size() < 30) {
            IntStream.range(0, 30).mapToObj(i -> DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -i), "yyyyMMdd"))
                    .filter(key -> flowExecutionMap.get(key) == null).forEach(key -> {
                        WorkflowChart chart = new WorkflowChart();
                        chart.setTime(key);
                        chart.setSuccessCount(0);
                        chart.setFailCount(0);
                        flowExecutionMap.put(key, chart);
                    });
        }
        return flowExecutionMap;
    }

    /**
     * generate execute every day map
     *
     * @param thirtyDaysFlowExecutions
     * @return
     */
    private Map<String, WorkflowChart> generateEveryDayFlowExecutionMap(List<FlowExecutionBase> thirtyDaysFlowExecutions) {
        Map<String, WorkflowChart> flowExecutionMap = new HashMap<>(30);
        if (CollectionUtils.isNotEmpty(thirtyDaysFlowExecutions)) {
            for (FlowExecutionBase flowExecution : thirtyDaysFlowExecutions) {
                String key = DateTimeUtils.formatLocalDateTime(flowExecution.getStartTime(), DateTimeUtils.DATETIME_DATA_PATTERN);
                WorkflowChart chart;
                if (MapUtils.isEmpty(flowExecutionMap) || flowExecutionMap.get(key) == null) {
                    chart = new WorkflowChart();
                    chart.setTime(key);
                    chart.setCount(1);
                    flowExecutionMap.put(key, chart);
                } else {
                    chart = flowExecutionMap.get(key);
                    chart.setCount(chart.getCount() + 1);
                    flowExecutionMap.put(key, chart);
                }
            }
        }
        if (flowExecutionMap.size() < 30) {
            IntStream.range(0, 30).mapToObj(i -> DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -i), "yyyyMMdd"))
                    .forEach(key -> {
                        WorkflowChart chart = new WorkflowChart();
                        chart.setTime(key);
                        chart.setCount(0);
                        flowExecutionMap.putIfAbsent(key, chart);
                    });
        }
        return flowExecutionMap;
    }

}