package io.innospots.workflow.console.service;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.console.model.flow.WorkflowChart;
import io.innospots.workflow.console.model.flow.WorkflowStatistics;
import io.innospots.workflow.console.operator.execution.ExecutionManagerOperator;
import io.innospots.workflow.core.execution.model.flow.FlowExecutionBase;
import io.innospots.workflow.core.execution.operator.IFlowExecutionOperator;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/9
 */
@Service
public class WorkflowExecutionService {

    private final IFlowExecutionOperator flowExecutionOperator;

    private final ExecutionManagerOperator executionManagerOperator;


    public WorkflowExecutionService(IFlowExecutionOperator flowExecutionOperator,
                                    ExecutionManagerOperator executionManagerOperator) {
        this.flowExecutionOperator = flowExecutionOperator;
        this.executionManagerOperator = executionManagerOperator;
    }

    public int deleteExecutionByFlowInstanceId(Long flowInstanceId) {
        return executionManagerOperator.deleteExecutionLog(flowInstanceId);
    }


    public WorkflowStatistics getWorkflowStat(Long workflowInstanceId) {
//        WorkflowInstance instance = workflowInstanceOperator.getWorkflowInstance(workflowInstanceId);
        String end = DateTimeUtils.formatDate(new Date(), "yyyyMMdd") + "999999.999";
        String start = DateTimeUtils.formatDate(DateUtils.addDays(new Date(), -30), "yyyyMMdd") + "000000.000";
//        PageBody<FlowExecutionBase> thirtyDaysBodies = flowExecutionOperator.pageFlowExecutions(workflowInstanceId, instance.getRevision(), start, end, null, null);
        PageBody<FlowExecutionBase> thirtyDaysBodies = flowExecutionOperator.pageFlowExecutions(workflowInstanceId, null, start, end, null, null);
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
//        PageBody<FlowExecutionBase> allBodies = flowExecutionOperator.pageFlowExecutions(workflowInstanceId, instance.getRevision(), null, null, null, null);
        PageBody<FlowExecutionBase> allBodies = flowExecutionOperator.pageFlowExecutions(workflowInstanceId, null, null, null, null, null);
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
