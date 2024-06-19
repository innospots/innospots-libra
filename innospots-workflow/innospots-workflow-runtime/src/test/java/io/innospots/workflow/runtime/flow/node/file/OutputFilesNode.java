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

package io.innospots.workflow.runtime.flow.node.file;

import cn.hutool.core.io.FileUtil;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.nio.file.StandardCopyOption;

/**
 * file output to target path that config in the node
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/22
 */
public class OutputFilesNode extends BaseNodeExecutor {

    public static final String FIELD_FILE_PATTERN = "file_pattern";

    public static final String FIELD_OUTPUT_PATH = "output_path";


    @Override
    protected void initialize() {
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        String filePattern = ni.valueString(FIELD_FILE_PATTERN);
        if(StringUtils.isEmpty(filePattern)){
            filePattern = ".*";
        }else if(filePattern.contains("*.")){
            filePattern=filePattern.replace("*.", ".*\\.");
        }

        File outDir = new File(this.valueString(FIELD_OUTPUT_PATH));
        if (!outDir.exists()) {
            outDir.mkdirs();
        }
        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            int pos  = 0;
            for (ExecutionInput executionInput : nodeExecution.getInputs()) {
                if (executionInput.isEmptyResource()) {
                    continue;
                }

                if(CollectionUtils.isNotEmpty(executionInput.getResources())){
                    for (ExecutionResource resource : executionInput.getResources()) {
                        if (!resource.getResourceName().matches(filePattern)) {
                            continue;
                        }
                        File outFile = new File(outDir, resource.getResourceName());
                        FileUtil.copyFile(resource.getLocalUri(), outFile.getAbsolutePath(), StandardCopyOption.REPLACE_EXISTING);
                        ExecutionResource outResource = ExecutionResource.buildResource(outFile, false, InnospotsWorkflowProperties.WORKFLOW_RESOURCES);
                        nodeOutput.addResource(pos,outResource);
                        if(CollectionUtils.isNotEmpty(executionInput.getData()) &&
                                executionInput.getData().size()-1 >= pos
                        ){
                            nodeOutput.addResult(executionInput.getData().get(pos));
                        }
                        pos++;
                    }//end for
                }


            }//end for execution input
        }//end if
    }
}
