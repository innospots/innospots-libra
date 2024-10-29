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

package io.innospots.workflow.node.app.logic;

import io.innospots.workflow.node.app.compute.ComputeField;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.node.app.connector.OutputFieldMode;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

import java.util.List;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/7
 */
public class LoopNode extends BaseNodeExecutor {
    private ParamField aggregateField;

    private List<ComputeField> computeFields;

    private OutputFieldMode outputFieldMode;

    private String outputField;

    public static final String FIELD_COMPUTE = "compute_fields";
    public static final String FIELD_AGGREGATE = "aggregate_field";
    public static final String FIELD_OUTPUT_MODE = "output_mode";
    public static final String FIELD_VARIABLE = "variable_name";


    @Override
    protected void initialize() {
    }

    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        //TODO
    }

}
