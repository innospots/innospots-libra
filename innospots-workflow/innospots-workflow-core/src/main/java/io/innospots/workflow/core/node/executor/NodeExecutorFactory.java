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

package io.innospots.workflow.core.node.executor;

import cn.hutool.core.util.StrUtil;
import io.innospots.base.script.ExecutorManagerFactory;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.workflow.core.exception.NodeBuildException;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.logger.FlowLoggerFactory;
import io.innospots.workflow.core.logger.IFlowLogger;
import lombok.extern.slf4j.Slf4j;

import java.lang.reflect.InvocationTargetException;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/12
 */
@Slf4j
public class NodeExecutorFactory {

    public static BaseNodeExecutor newInstance(NodeInstance nodeInstance) throws ClassNotFoundException, IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        BaseNodeExecutor baseNode = null;
        baseNode = (BaseNodeExecutor) Class.forName(nodeInstance.getNodeType()).getDeclaredConstructor().newInstance();
        baseNode.ni = nodeInstance;
        return baseNode;
    }

    public static BaseNodeExecutor build(String flowIdentifier, NodeInstance nodeInstance) {
        BaseNodeExecutor nodeExecutor;
        IFlowLogger flowLogger = FlowLoggerFactory.getLogger();
        try {
            nodeExecutor = newInstance(nodeInstance);
            nodeExecutor.flowIdentifier = flowIdentifier;
            nodeExecutor.flowLogger = flowLogger;
            nodeExecutor.build();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            flowLogger.flowError("node instance fail, flowKey:{}, nodeCode:{} ,nodeType: {}, error:{}",
                    flowIdentifier,nodeInstance.getCode(),nodeInstance.getNodeType(),e.getMessage());
            throw NodeBuildException.build(nodeInstance.getNodeType(),e);
        }
        return nodeExecutor;
    }

        /**
     * Compiles and constructs a node executor based on the provided identifier and node instance.
     *
     * @param identifier A unique identifier for the node, used to fetch the corresponding executor manager.
     * @param nodeInstance The instance of the node that needs to be compiled and constructed.
     * @return The compiled and constructed node executor.
     *
     * This method retrieves the executor manager by the given identifier, initializes a node executor from the node instance,
     * compiles the script method body for the executor, registers it with the executor manager if not null, and proceeds
     * to build both the executor manager and the node executor itself. In case of exceptions during this process, a
     * NodeBuildException is thrown encapsulating the error details.
     */
    public static BaseNodeExecutor compileBuild(String identifier, NodeInstance nodeInstance) {
        BaseNodeExecutor nodeExecutor;
        // Retrieves the executor manager corresponding to the identifier
        ScriptExecutorManager executorManager = ExecutorManagerFactory.getInstance(identifier);
        IFlowLogger flowLogger = FlowLoggerFactory.getLogger();
        try {
            // Initializes the node executor instance
            nodeExecutor = newInstance(nodeInstance);
            // Sets the flow identifier for the node executor
            nodeExecutor.flowIdentifier = identifier;
            nodeExecutor.flowLogger = flowLogger;
            // Constructs the method body for the script
            MethodBody methodBody = nodeExecutor.buildScriptMethodBody();
            // Registers the method body with the executor manager if available
            if(methodBody != null) {
                executorManager.register(methodBody);
            }
            // Executes build and reload operations on the executor manager to ensure readiness
            executorManager.build();
            executorManager.reload();
            // Completes the construction of the node executor
            nodeExecutor.build();
            //ExecutorManagerFactory.clear(identifier);
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            // Throws a NodeBuildException with the node type and underlying exception details
            flowLogger.flowError("node instance fail, flowKey:{}, nodeCode:{} ,nodeType: {}, error:{}",
                    identifier,nodeInstance.getCode(),nodeInstance.getNodeType(),e.getMessage());
            throw NodeBuildException.build(nodeInstance.getNodeType(), e);
        }
        // Returns the completed node executor
        return nodeExecutor;
    }


}
