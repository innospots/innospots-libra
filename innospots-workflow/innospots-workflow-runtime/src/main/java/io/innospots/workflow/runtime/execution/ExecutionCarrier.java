package io.innospots.workflow.runtime.execution;

import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.TriggerNode;
import io.innospots.workflow.runtime.engine.ParallelStreamFlowEngine;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/16
 */
@Slf4j
public class ExecutionCarrier {

    public static final int MAX_TIMES = 1000;

    private final ThreadTaskExecutor taskExecutor;

    private Map<String, EcUnit> executionUnits = new ConcurrentHashMap<>();

    private TimeoutTask timeoutTask;

    Flow flow;

    FlowExecution flowExecution;

    private ExecutionCarrier(Flow flow, FlowExecution flowExecution, ThreadTaskExecutor taskExecutor) {
        this.flow = flow;
        this.flowExecution = flowExecution;
        this.taskExecutor = taskExecutor;
        this.timeoutTask = new TimeoutTask(this);
    }

    public static ExecutionCarrier build(Flow flow, FlowExecution flowExecution, ThreadTaskExecutor taskExecutor) {
        return new ExecutionCarrier(flow, flowExecution, taskExecutor);
    }


    public void execute() {
        List<BaseNodeExecutor> nodeExecutors = null;
        if (CollectionUtils.isEmpty(flowExecution.getCurrentNodeKeys())) {
            nodeExecutors = flow.startNodes();
        } else {
            nodeExecutors = flow.findNodes(flowExecution.getCurrentNodeKeys());
        }
        if (nodeExecutors == null) {
            flowExecution.setStatus(ExecutionStatus.FAILED);
            flowExecution.setMessage("start node is null");
            return;
        }
        if (nodeExecutors.size() == 1) {
            runNodeExecutor(nodeExecutors.get(0), false);
        } else {
            for (BaseNodeExecutor nodeExecutor : nodeExecutors) {
                if (!this.shouldExecute(nodeExecutor.nodeKey())) {
                    continue;
                }
                if (nodeExecutor instanceof TriggerNode) {
                    runNodeExecutor(nodeExecutor, false);
                } else {
                    runNodeExecutor(nodeExecutor, true);
                }
            }
        }

        scanDone();
    }

    void scanDone() {
        boolean isDone;
        int times = 0;
        int count = 0;
        do {
            isDone = true;
            count = 0;
            for (EcUnit unit : executionUnits.values()) {
                if (unit.unitStatus() == ExecutionStatus.READY) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(5);
                    } catch (InterruptedException e) {
                        log.error(e.getMessage());
                    }
                }
                isDone = unit.isDone() && isDone;
                if (log.isDebugEnabled()) {
                    log.debug("unit output:{},{},{}", unit.nodeKey(), unit.unitStatus(), unit.isTaskDone());
                }
                count++;
            }
            if (flowExecution.shouldStopped()) {
                if (!isDone) {
                    List<EcUnit> ll = executionUnits.values().stream()
                            .filter(unit -> !unit.unitStatus().isDone())
                            .collect(Collectors.toList());
                    log.warn("flow execution has been stopped, not all of node have executed completely,{}", ll);
                }
                break;
            }
            times++;
        } while ((!isDone || count < executionUnits.size()) && times < MAX_TIMES);
        timeoutTask.interrupt();

        if (log.isDebugEnabled()) {
            log.debug("times:{}, unitCount:{},size:{}, flow execution: {}", times, count, executionUnits.size(), flowExecution);
        }
    }

    void addUnit(EcUnit ecUnit) {
        executionUnits.put(ecUnit.nodeKey(), ecUnit);
    }

    boolean shouldStopped() {
        return flowExecution.shouldStopped();
    }

    void runNodeExecutor(BaseNodeExecutor nodeExecutor, boolean async) {
        EcUnit ecUnit = EcUnit.build(nodeExecutor, this);
        runUnit(ecUnit, async);
    }

    void runNodeExecutor(String nodeKey, boolean async) {
        if (hasExecuted(nodeKey)) {
            log.warn("node has executed, nodeKey:{}", nodeKey);
            return;
        }
        BaseNodeExecutor nodeExecutor = flow.findNode(nodeKey);
        EcUnit ecUnit = executionUnits.get(nodeKey);
        if (ecUnit == null) {
            ecUnit = EcUnit.build(nodeExecutor, this);
            runUnit(ecUnit, async);
        } else if (ecUnit.nodeExecution == null && !ecUnit.unitStatus.isDone()) {
            runUnit(ecUnit, async);
        } else {
            log.warn("node has bean executed, node key:{}", nodeKey);
        }
    }

    /**
     * run flow node
     *
     * @param ecUnit unit
     * @param async async
     */
    void runUnit(EcUnit ecUnit, boolean async) {
        if (shouldStopped()) {
            return;
        }
        CompletableFuture<NodeExecution> future = null;
        if (async) {
            future = CompletableFuture.supplyAsync(ecUnit::execute, taskExecutor);
            ecUnit.future = future;
        } else {
            future = CompletableFuture.completedFuture(ecUnit.execute());
        }
        if (log.isDebugEnabled()) {
            log.debug("run node:{}, async:{}, isDone:{}, hashcode:{}", ecUnit.nodeKey(), async, future.isDone(), future.hashCode());
        }
    }

    boolean hasExecuted(String nodeKey) {
        return this.flowExecution.isExecuted(nodeKey);
    }

    /**
     * flow execution has been set stopped flag ,which will be stopped, when executing to this node
     *
     * @param nodeKey key
     * @return stopped
     */
    boolean isStoppedNode(String nodeKey) {
        return flowExecution.getEndNodeKey() != null &&
                flowExecution.getEndNodeKey().equals(nodeKey);
    }

    boolean hasDone(String nodeKey) {
        return flowExecution.isDone(nodeKey);
    }

    public boolean shouldExecute(String nodeKey) {
        return flowExecution.shouldExecute(nodeKey);
    }

    NodeExecution getNodeExecution(String nodeKey) {
        return flowExecution.getNodeExecution(nodeKey);
    }

    Set<String> sourceKey(String nodeKey) {
        return flow.sourceKey(nodeKey);
    }

    List<String> previousNotExecuteNodes(BaseNodeExecutor nodeExecutor) {
        //whether all the source nodes are complete
        Set<String> previousNodeKeys = flow.sourceKey(nodeExecutor.nodeKey());
        if (log.isDebugEnabled()) {
            log.debug("current node: {} ,previous nodes: {}", nodeExecutor.simpleInfo(), previousNodeKeys);
        }
        if (CollectionUtils.isEmpty(previousNodeKeys)) {
            return Collections.emptyList();
        }
        //not execute node list
        List<String> unDoneList = new ArrayList<>();

        for (String source : previousNodeKeys) {
            if (!this.hasDone(source) && this.shouldExecute(source)) {
                unDoneList.add(source);
            }
        }//end for

        return unDoneList;
    }

    EcUnit getEcUnit(String nodeKey) {
        return this.executionUnits.get(nodeKey);
    }


    private static final class TimeoutTask extends Thread {

        private final ExecutionCarrier carrier;
        private boolean running = false;

        public TimeoutTask(ExecutionCarrier carrier) {
            this.carrier = carrier;
        }

        @SneakyThrows
        @Override
        public void run() {
            this.running = true;
            long startTime = System.currentTimeMillis();

            int totalCount = carrier.executionUnits.size();
            if (log.isDebugEnabled()) {
                log.debug("start timeout task:{}, unit size:{}", carrier.flowExecution.getFlowExecutionId(), totalCount);
            }
            int doneCount = 0;
            try {
                while (running && doneCount < totalCount) {
                    totalCount = 0;
                    doneCount = 0;
                    for (EcUnit ecUnit : carrier.executionUnits.values()) {
                        totalCount++;
                        if (ecUnit.isTaskDone()) {
                            doneCount++;
                        }
                        if (ecUnit.isTimeout()) {
                            ecUnit.cancel();
                        }
                        TimeUnit.MILLISECONDS.sleep(5);
                    }//end for
                }//end while
            } catch (Exception e) {
                log.error(e.getMessage());
            }

            if (log.isDebugEnabled()) {
                log.debug("execution unit:{}", carrier.executionUnits.keySet());
                String consume = DateTimeUtils.consume(startTime);
                log.debug("timeout task consume:{} unit done:{}, total:{}", consume, doneCount, totalCount);
            }
        }

        public void close() {
            this.running = false;
        }
    }

}
