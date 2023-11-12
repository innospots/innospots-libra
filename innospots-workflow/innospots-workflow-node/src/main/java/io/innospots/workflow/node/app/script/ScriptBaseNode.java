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

package io.innospots.workflow.node.app.script;


import cn.hutool.core.exceptions.ExceptionUtil;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.re.ExpressionEngineFactory;
import io.innospots.base.re.IExpression;
import io.innospots.base.re.IExpressionEngine;
import io.innospots.base.re.jit.MethodBody;
import io.innospots.workflow.core.enums.BuildStatus;
import io.innospots.workflow.core.execution.AsyncExecutors;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.enums.ExecutionStatus;
import io.innospots.workflow.core.execution.events.NodeExecutionTaskEvent;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.listener.INodeExecutionListener;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.executor.INodeExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.InvocationTargetException;
import java.util.*;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public abstract class ScriptBaseNode extends BaseNodeExecutor {


    private static final Logger logger = LoggerFactory.getLogger(ScriptBaseNode.class);


    protected IExpression<Object> expression;

    protected Map<String, IExpression<Object>> actionScripts;

    protected void initialize() {
    }

    /*
    protected void buildExpression(String flowIdentifier, NodeInstance nodeInstance) throws ScriptException {
        List<MethodBody> methodBodies = nodeInstance.expMethods();
        if (methodBodies.isEmpty()) {
            return;
        }
        this.actionScripts = new HashMap<>();
        for (MethodBody methodBody : methodBodies) {
            IExpressionEngine expressionEngine = ExpressionEngineFactory.getEngine(flowIdentifier, methodBody.getScriptType());
            if (expressionEngine == null) {
                logger.error("expression engine is null, identifier:{}, scriptType:{}", flowIdentifier, methodBody.getScriptType());
                this.buildStatus = BuildStatus.FAIL;
                continue;
            }
            IExpression exp = expressionEngine.getExpression(methodBody.getMethodName());
            if (NodeInstance.FIELD_DEFAULT_ACTION.equals(methodBody.getFormName())) {
                this.expression = exp;
            } else {
                this.actionScripts.put(methodBody.getFormName(), exp);
            }
        }
    }

     */


}
