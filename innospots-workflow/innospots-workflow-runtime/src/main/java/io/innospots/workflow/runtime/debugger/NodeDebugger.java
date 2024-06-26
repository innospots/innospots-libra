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

package io.innospots.workflow.runtime.debugger;

import cn.hutool.core.io.IoUtil;
import cn.hutool.crypto.digest.DigestUtil;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ExecutorManagerFactory;
import io.innospots.base.utils.FileUtils;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.debug.DebugPayload;
import io.innospots.workflow.core.debug.DebugInput;
import io.innospots.workflow.core.execution.enums.RecordMode;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.flow.FlowExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.flow.manage.FlowCompiler;
import io.innospots.workflow.core.flow.model.WorkflowBaseBody;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import io.innospots.workflow.node.app.script.ScriptNode;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/4/23
 */
@Slf4j
public class NodeDebugger {


    /**
     * upload test file to temp directory
     * @param uploadFile
     * @return
     */
    public static ExecutionResource updateTestFile(MultipartFile uploadFile,boolean force){
        ExecutionResource res = null;
        try {
            Path parentPath = Files.createTempDirectory("innospots_files");
//            Path parentPath = Path.of("/Users/yxy/temp/abc1");
            File pPath = parentPath.toFile();
            res = FileUtils.getInstance().upload(uploadFile.getInputStream(),
                    pPath.getAbsolutePath(), uploadFile.getOriginalFilename(),
                    InnospotsWorkflowProperties.WORKFLOW_RESOURCES, force);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        log.info("upload file:{}",res);
        return res;
    }

    public static NodeExecutionDisplay execute(DebugPayload debugPayload){
        Integer id = debugPayload.getNi().getNodeDefinitionId();
        NodeInstance ni = debugPayload.getNi();
        if(StringUtils.isEmpty(ni.getNodeKey())){
            ni.setNodeKey("nodeKey_"+id);
        }
        String identifier = "NodeDebug"+debugPayload.getNi().getCode()+"_"+ id;
        if(StringUtils.isEmpty(ni.getNodeType())){
            ni.setNodeType(ScriptNode.class.getName());
        }

        BaseNodeExecutor nodeExecutor = NodeExecutorFactory.compileBuild(identifier,ni);


        FlowExecution flowExecution = FlowExecution.buildNewFlowExecution(1L,0);
        flowExecution.setRecordMode(RecordMode.SYNC);

        NodeExecution nodeExecution = NodeExecution.buildNewNodeExecution(ni.getNodeKey(),1L,1,identifier,true);
        nodeExecution.setRecordMode(RecordMode.SYNC);
        List<ExecutionInput> inputs = new ArrayList<>();
        for (DebugInput input : debugPayload.getInputs()) {
            ExecutionInput executionInput = new ExecutionInput();
            executionInput.setResources(input.getResources());
            executionInput.setData(JSONUtils.toMapList(input.getData(),Map.class));
            inputs.add(executionInput);
        }
        nodeExecution.setInputs(inputs);
        nodeExecutor.innerExecute(nodeExecution,flowExecution);

        return NodeExecutionDisplay.build(nodeExecution,ni);
    }
}
