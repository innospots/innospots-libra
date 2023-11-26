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

import io.innospots.workflow.core.exception.NodeBuildException;
import io.innospots.workflow.core.instance.model.NodeInstance;
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
        try {
            nodeExecutor = newInstance(nodeInstance);
            nodeExecutor.flowIdentifier = flowIdentifier;
            nodeExecutor.build();
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw NodeBuildException.build(nodeInstance.getNodeType(),e);
        }
        return nodeExecutor;
    }

}
