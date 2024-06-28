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

package io.innospots.workflow.core.flow.manage;

import com.fasterxml.jackson.databind.json.JsonMapper;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.script.ExecutorManagerFactory;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.flow.loader.FsWorkflowLoader;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.MalformedURLException;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/5
 */
@Slf4j
public class FlowCompiler {

    private WorkflowBaseBody workflowInstance;

    private ScriptExecutorManager scriptExecutorManager;

//    public Map<String, IScriptExecutorManager> scriptEngines = new HashMap<>();

    private FlowCompiler(WorkflowBaseBody workflowInstance) {
        this(workflowInstance.identifier());
        this.workflowInstance = workflowInstance;
    }

    private FlowCompiler(String identifier){
        scriptExecutorManager = ExecutorManagerFactory.getInstance(identifier);
//        scriptExecutorManager = ScriptExecutorManager.newInstance(identifier);
//                ExecutorManagerFactory.build(identifier);
    }

    public static FlowCompiler build(String identifier){
        return new FlowCompiler(identifier);
    }

    public static FlowCompiler build(WorkflowBaseBody workflowInstance){
        return new FlowCompiler(workflowInstance);
    }

    public void clear() {
        scriptExecutorManager.clear();
        ExecutorManagerFactory.clear(scriptExecutorManager.identifier());
    }

    public boolean isCompiled() {
        try {
            scriptExecutorManager.classForName();
            return true;
        } catch (ClassNotFoundException | MalformedURLException e) {
            log.warn("class exception:{},{},{}", e.getClass().getName(), e.getMessage(), workflowInstance.identifier());
            return false;
        }
    }

    public void compile() throws ScriptException {
        if(workflowInstance!=null){
            for (NodeInstance node : workflowInstance.getNodes()) {
                registerToEngine(node);
            }//end for
        }
        scriptExecutorManager.build();
//        scriptEngines.values().stream().filter(Objects::nonNull).forEach(IScriptExecutorManager::build);
        outputFlowFile(ScriptExecutorManager.getClassPath() + File.separator + InnospotsWorkflowProperties.FLOW_JSON_PATH);
    }


    /**
     * build node instance
     *
     * @param nodeInstance
     * @return
     */
    private void registerToEngine(NodeInstance nodeInstance) {
        BaseNodeExecutor nodeExecutor = null;
        try {
            nodeExecutor = NodeExecutorFactory.newInstance(nodeInstance);
            MethodBody methodBody = nodeExecutor.buildScriptMethodBody();
            if (methodBody!= null) {
                scriptExecutorManager.register(methodBody);
            }
        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error(e.getMessage()+","+e.getClass().getName());
            throw InnospotException.buildException(BaseNodeExecutor.class, ResponseCode.INITIALIZING, e);
        }
//        List<MethodBody> methodBodies = null;
//                nodeInstance.expMethods();

//        for (MethodBody methodBody : methodBodies) {
//            String scriptType = methodBody.getScriptType();
//            IScriptExecutorManager expressionEngine;
            /*
            if (scriptType == null || scriptType == ScriptType.JAVA || scriptType == ScriptType.JAVASCRIPT) {
                expressionEngine = genericExpressionEngine;
            }else{
                expressionEngine = scriptEngines.get(scriptType.name());
                if(expressionEngine == null){
                    expressionEngine = ExpressionEngineFactory.build(genericExpressionEngine.identifier(), scriptType);
                    scriptEngines.put(scriptType.name(),expressionEngine);
                }
            }


            if (expressionEngine != null) {
                expressionEngine.register(methodBody);
            }             */
//        }//end for methodBody
//        return nodeExecutor;
    }

    /**
     * write workflow instance json to file
     *
     * @param outputPath
     */
    private void outputFlowFile(String outputPath) {
        if(workflowInstance==null){
            return;
        }
        File dir = new File(outputPath);
        if (!dir.exists()) {
            dir.mkdirs();
        }
        File confFile = new File(dir, workflowInstance.identifier() + ".json");
        if (confFile.exists()) {
            confFile.delete();
        }
        log.info("output workflow instance config:{}", confFile.getAbsolutePath());
        try {
            JsonMapper jsonMapper = JsonMapper.builder().findAndAddModules().build();
            jsonMapper.writeValue(confFile, workflowInstance);
        } catch (IOException e) {
            throw ResourceException.buildIOException(this.getClass(), e);
        }
    }

}
