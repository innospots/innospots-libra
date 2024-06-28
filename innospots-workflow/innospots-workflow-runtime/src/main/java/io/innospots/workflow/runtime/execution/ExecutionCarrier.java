package io.innospots.workflow.runtime.execution;

import cn.hutool.core.util.ArrayUtil;
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

    Flow flow;

    FlowExecution flowExecution;

    private ExecutionCarrier(Flow flow, FlowExecution flowExecution, ThreadTaskExecutor taskExecutor) {
        this.flow = flow;
        this.flowExecution = flowExecution;
        this.taskExecutor = taskExecutor;

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

        waitForComplete();
    }

    void waitForComplete() {
        boolean isDone;
        int times = 0;
        int count = 0;
        do {
            isDone = true;
            count = 0;
            for (EcUnit unit : executionUnits.values()) {
                if (unit.unitStatus() == ExecutionStatus.READY) {
                    try {
                        TimeUnit.MILLISECONDS.sleep(1);
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

    EcUnit runNodeExecutor(BaseNodeExecutor nodeExecutor, boolean async) {
        EcUnit ecUnit = EcUnit.build(nodeExecutor, this);
        runUnitAndPrevious(ecUnit, async, true);
        return ecUnit;
    }

    EcUnit runNodeExecutor(String nodeKey, boolean async, boolean runNext) {
        if (hasExecuted(nodeKey)) {
            log.warn("node has executed, nodeKey:{}", nodeKey);
            return null;
        }

        BaseNodeExecutor nodeExecutor = flow.findNode(nodeKey);
        EcUnit ecUnit = executionUnits.get(nodeKey);
        if (ecUnit == null) {
            ecUnit = EcUnit.build(nodeExecutor, this);
            runUnitAndPrevious(ecUnit, async, runNext);
        } else if (ecUnit.nodeExecution == null && !ecUnit.unitStatus.isDone()) {
            runUnitAndPrevious(ecUnit, async, runNext);
        } else {
            log.warn("node has bean executed, node key:{}", nodeKey);
        }
        return ecUnit;
    }

    void runUnitAndPrevious(EcUnit ecUnit, boolean async, boolean runNext) {
        CompletableFuture cf = executePreviousNodes(ecUnit);
        cf.thenAccept(p -> runUnit(ecUnit, async, runNext));
    }


    /**
     * run flow node
     *
     * @param ecUnit unit
     * @param async  async
     */
    void runUnit(EcUnit ecUnit, boolean async, boolean runNext) {
        if (shouldStopped()) {
            return;
        }
        CompletableFuture<NodeExecution> future = null;
        if (async) {
            future = CompletableFuture.supplyAsync(ecUnit::execute, taskExecutor);
            ecUnit.future = future;
        } else {
            future = CompletableFuture.completedFuture(ecUnit.execute());
            ecUnit.future = future;
        }
        if (log.isDebugEnabled()) {
            log.debug("run node:{}, async:{}, isDone:{}, hashcode:{}", ecUnit.nodeKey(), async, future.isDone(), future.hashCode());
        }

        if (runNext) {
            future.thenAccept(this::runNextNodes);
        }
    }

    private CompletableFuture executePreviousNodes(EcUnit ecUnit) {
        CompletableFuture prev = null;
        //not execute node list
        List<String> unDoneList = this.previousNotExecuteNodes(ecUnit.nodeExecutor);

        if (unDoneList.isEmpty()) {
            return CompletableFuture.completedFuture(null);
        }

        if (log.isDebugEnabled()) {
            log.debug("node: {}, undone nodes: {}, previous nodes: {}", ecUnit.nodeExecutor.simpleInfo(), unDoneList, this.sourceKey(ecUnit.nodeKey()));
        }
        boolean allDone = true;

        //have not execute source nodes in the current node

        if (unDoneList.size() == 1) {
            this.runNodeExecutor(unDoneList.get(0), false, false);
        } else {
            for (String unDoneNode : unDoneList) {
                //recursively invoke the node that needs to be executed in the unDoneList
                this.runNodeExecutor(unDoneNode, true, false);
            }
        }

        List<CompletableFuture> cfs = new ArrayList<>();
        for (String unDoneNode : unDoneList) {
            EcUnit unit = this.getEcUnit(unDoneNode);
            if (unit == null) {
                continue;
            }
            cfs.add(unit.future);
        }//end for

        prev = CompletableFuture.allOf(ArrayUtil.toArray(cfs, CompletableFuture.class));

        return prev;
    }

    /**
     * flow next node
     *
     * @param nodeExecution
     */
    private void runNextNodes(NodeExecution nodeExecution) {
        List<String> nextNodes = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(nodeExecution.getNextNodeKeys())) {
            nextNodes.addAll(nodeExecution.getNextNodeKeys());
            nextNodes = nextNodes.stream().filter(this::shouldExecute).collect(Collectors.toList());
        }

        if (nextNodes.isEmpty()) {
            if (log.isDebugEnabled()) {
                log.debug("the node is the leaf node, node key:{}", nodeExecution.getNodeKey());
            }
            return;
        }

        //only one next node
        if (nextNodes.size() == 1) {
            this.runNodeExecutor(nextNodes.get(0), false, true);
        } else {
            //parallel execute
            for (String nextNode : nextNodes) {
                if (this.hasDone(nextNode)) {
                    //log.error("The flow is a directed acyclic graph, which has the loop node, please check the flow node config, the loop node key:{}",nextNode);
                    log.warn("next nodes has bean executed, node key:{}", nextNode);
                    continue;
                }
                this.runNodeExecutor(nextNode, true, true);
            }
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


}
