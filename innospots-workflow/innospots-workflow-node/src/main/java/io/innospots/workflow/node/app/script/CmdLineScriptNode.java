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

package io.innospots.workflow.node.app.script;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.OutputMode;
import io.innospots.base.script.cmdline.CmdLineScriptExecutor;
import io.innospots.base.script.jit.MethodBody;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/11
 */
@Slf4j
public class CmdLineScriptNode extends ScriptBaseNode {

    protected static final String FIELD_OUTPUT_MODE = "output_mode";
    protected static final String FIELD_CMD_PATH = "cmd_path";
    protected static final String FIELD_VARIABLE_NAME = "variable_name";
    protected static final ObjectMapper OBJECT_MAPPER = JSONUtils.mapper();

    protected OutputMode outputMode;
    protected String outputField;
    protected String cmdPath;

    @Override
    protected void initialize() {
        super.initialize();
        outputMode = OutputMode.valueOf(validString(FIELD_OUTPUT_MODE));
        cmdPath = valueString(FIELD_CMD_PATH);
        if (outputMode == OutputMode.FIELD) {
            outputField = validString(FIELD_VARIABLE_NAME);
        }
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());

        if (scriptExecutor != null) {
            StringBuilder msg = new StringBuilder();
            if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
                for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                    if (CollectionUtils.isNotEmpty(executionInput.getData())) {
                        for (Map<String, Object> data : executionInput.getData()) {
                            executeItem(data, msg, nodeExecution, nodeOutput);
                        }//end for
                    } else {
                        executeItem(new HashMap<>(10), msg, nodeExecution, nodeOutput);
                    }
                }//end execution input
            } else {
                executeItem(new HashMap<>(10), msg, nodeExecution, nodeOutput);
            }
            if (msg.length() > 65000) {
                nodeExecution.setMessage(msg.substring(0, 65000));
            } else {
                nodeExecution.setMessage(msg.toString());
            }

        } else {//end if
            nodeOutput.addNextKey(this.nextNodeKeys());
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                nodeOutput.addResult(executionInput.getData());
            }//end execution input
        }
        nodeExecution.addOutput(nodeOutput);
    }

    private void executeItem(Map<String, Object> item, StringBuilder msg,
                             NodeExecution nodeExecution, NodeOutput nodeOutput) {
        CmdLineScriptExecutor cmdLineScriptExecutor = (CmdLineScriptExecutor) scriptExecutor;
        Object result = cmdLineScriptExecutor.execute(item, (line) -> {
            if (this.outputMode == OutputMode.STREAM) {
                if (StringUtils.isNotEmpty(line)) {
                    flowLogger.flowInfo(nodeExecution.getFlowExecutionId(), line);
                    msg.append(line).append("\n");
                }
            }
            return line;

        });

        processOutput(nodeExecution, result, item, nodeOutput);

        if (this.outputMode == OutputMode.LOG) {
            msg.append(result).append("\n");
        }
    }

    protected void processOutput(NodeExecution nodeExecution, Object result, Map<String, Object> input, NodeOutput nodeOutput) {
        if (log.isDebugEnabled()) {
            log.debug("nodeKey:{}, script output:{}", this.nodeKey(), result);
        }
        if (this.outputMode == OutputMode.FIELD) {
            Map<String, Object> data = new LinkedHashMap<>(10);
            if (MapUtils.isNotEmpty(input)) {
                data.putAll(input);
                nodeOutput.addResult(input);
            }
            result = parseObject(result);
            if (result != null) {
                data.put(this.outputField, result);
            }
            super.processOutput(nodeExecution, data, nodeOutput);
        } else if (this.outputMode == OutputMode.OVERWRITE) {
            result = parseObject(result);
            super.processOutput(nodeExecution, result, nodeOutput);
        } else if (this.outputMode == OutputMode.PAYLOAD) {
            result = parseObject(result);
            Map<String, Object> data = new LinkedHashMap<>(10);
            if (MapUtils.isNotEmpty(input)) {
                data.putAll(input);
                nodeOutput.addResult(input);
            }
            if (result instanceof Map) {
                data.putAll((Map) result);
            } else {
                data.put(this.nodeKey(), result);
            }

            super.processOutput(nodeExecution, data, nodeOutput);
        }
    }


    private Object parseObject(Object result) {
        try {
            if (result instanceof String) {
                if (((String) result).startsWith("[")) {
                    CollectionType listType = OBJECT_MAPPER.getTypeFactory().constructCollectionType(ArrayList.class, Map.class);
                    result = OBJECT_MAPPER.readValue((String) result, listType);
                } else if (((String) result).startsWith("{")) {
                    result = OBJECT_MAPPER.readValue((String) result, Map.class);
                }
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return result;
    }

    @Override
    public MethodBody buildScriptMethodBody() {
        String src = this.valueString(FIELD_ACTION_SCRIPT);
        OutputMode oMode = OutputMode.valueOf(this.valueString(FIELD_OUTPUT_MODE));
        String scriptType = scriptType();
        if (StringUtils.isEmpty(src) || StringUtils.isEmpty(scriptType)) {
            return null;
        }

        MethodBody methodBody = new MethodBody();
        methodBody.setOutputMode(oMode);
        String cmdPath = this.valueString(FIELD_CMD_PATH);
        methodBody.setCmdPath(cmdPath);
        methodBody.setReturnType(Object.class);
        methodBody.setScriptType(scriptType);
        methodBody.setMethodName(ni.expName());

        methodBody.setSrcBody(src);
        return methodBody;
    }



}
