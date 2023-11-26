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


import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.ExecutorManagerFactory;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Slf4j
public abstract class ScriptBaseNode extends BaseNodeExecutor {

    protected IScriptExecutor scriptExecutor;

    protected void initialize() {
       ScriptExecutorManager executorManager = ExecutorManagerFactory.getCache(this.flowIdentifier);
       if(executorManager==null){
           throw ScriptException.buildExecutorException(this.getClass(),this.scriptType(),"script executor manager is null,",this.flowIdentifier);
       }
        scriptExecutor = executorManager.getExecutor(this.ni.expName());
       if(scriptExecutor==null){
           throw ScriptException.buildExecutorException(this.getClass(),this.scriptType(),"script executor is null,",this.flowIdentifier);
       }
    }

    @Override
    protected Object processItem(Map<String, Object> item) {
        if(scriptExecutor!=null){
            return scriptExecutor.execute(item);
        }
        return item;
    }
}
