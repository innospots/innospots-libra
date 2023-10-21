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

package io.innospots.workflow.node.app.file;

import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.ExecutionResource;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

/**
 * load files from target path and return the file info
 * that include file type, file path and file size etc.
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/22
 */
public class LoadFilesNode extends BaseAppNode {

    public static final String FIELD_SOURCE_FILES = "source_files";
    public static final String FIELD_FILE_VAR = "file_var";

    private String sourceFiles;
    private String fileVar;

    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(FIELD_SOURCE_FILES);
        //validFieldConfig(FIELD_FILE_VAR);
        sourceFiles = nodeInstance.valueString(FIELD_SOURCE_FILES);
        fileVar = nodeInstance.valueString(FIELD_FILE_VAR);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        // eg: /tmp/*.img
        File[] readFiles = selectFiles(sourceFiles);
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        if (readFiles != null) {
            for (int i = 0; i < readFiles.length; i++) {
                File rFile = readFiles[i];
                ExecutionResource executionResource = ExecutionResource.buildResource(rFile, true);
                nodeOutput.addResource(i,executionResource);
                if(StringUtils.isNotEmpty(fileVar)){
                    Map<String, Object> files = new HashMap<>();
                    files.put(fileVar,executionResource.toMetaInfo());
                    nodeOutput.addResult(files);
                }else{
                    nodeOutput.addResult(executionResource.toMetaInfo());
                }

            }//end for
        }//end if
    }

    protected File[] selectFiles(String sourceDir) {
        File sourceFile = new File(sourceDir);
        File[] files;
        if (sourceFile.isDirectory()) {
            files = sourceFile.listFiles();
        } else if (sourceFile.isFile()) {
            files = new File[]{sourceFile};
        } else {
            File parent = sourceFile.getParentFile();
            String fileName = sourceFile.getName().replace(".", "\\.").replace("*", ".*");
            files = parent.listFiles((dir, name) -> name.matches(fileName));
        }

        return files;
    }
}
