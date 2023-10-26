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

package io.innospots.workflow.core.execution.reader;

import io.innospots.base.utils.BeanContextAware;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.execution.operator.IExecutionContextOperator;

import java.util.List;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/1/23
 */
public class NodeOutputStaticReader {

    private static IExecutionContextOperator executionContextOperator;

    public static List<NodeOutput> readNodeOutputs(String flowExecutionId,
                                                   String nodeExecutionId,
                                                   String targetNodeKey) {

        if (executionContextOperator == null) {
            executionContextOperator = BeanContextAware.getBean(IExecutionContextOperator.class);
        }
        return executionContextOperator.readNodeOutputs(flowExecutionId, nodeExecutionId, targetNodeKey);
    }

}
