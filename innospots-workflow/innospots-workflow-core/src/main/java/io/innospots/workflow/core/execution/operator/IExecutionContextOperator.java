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

package io.innospots.workflow.core.execution.operator;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.ExecutionResource;
import io.innospots.workflow.core.execution.flow.FlowExecution;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.InputStreamSource;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.*;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

/**
 * /storePath/flowKey/yyyyMMdd/flowExecutionId/file
 * /storePath/flowKey/yyyyMMdd/flowExecutionId/nodeExecutionId~input-items.f.gz
 * /storePath/flowKey/yyyyMMdd/flowExecutionId/nodeExecutionId~output-items.f.gz
 *
 * @author Smars
 * @version 1.2.0
 * @date 2023/1/19
 */
public interface IExecutionContextOperator {

    Logger logger = LoggerFactory.getLogger(IExecutionContextOperator.class);

    String INPUT_FILE_SUFFIX = "input-items-";

    String OUTPUT_FILE_SUFFIX = "output-items-";

    String FILE_MIME_TYPE = ".f.gz";
    String DIR_ATTACHMENT = "files";

    List<NodeOutput> readNodeOutputs(String flowExecutionId, String nodeExecutionId);

    /**
     * 读取执行节点输出结果，向后输出到目标
     *
     * @param flowExecutionId
     * @param nodeExecutionId
     * @param targetNodeKey
     * @return
     */
    List<NodeOutput> readNodeOutputs(String flowExecutionId, String nodeExecutionId, String targetNodeKey);


    Map<String, Object> readFlowExecutionContext(String flowExecutionId);

    PageBody<NodeOutput> pageNodeOutputs(String executionId, int page, int size);


    void saveExecutionContext(NodeExecution nodeExecution);

    void saveExecutionContext(FlowExecution flowExecution);

    void cleanExecutionContext(Long flowInstanceId);

    default void fillNodeExecution(NodeExecution nodeExecution, boolean fillOutput) {
        fillNodeExecution(nodeExecution, fillOutput, 0, Integer.MAX_VALUE);
    }

    void fillNodeExecution(NodeExecution nodeExecution, boolean fillOutput, int page, int size);

    void fillFlowExecution(FlowExecution flowExecution, boolean fillOutput);

    default void fillNodeExecutionOutput(NodeExecution nodeExecution) {
        fillNodeExecutionOutput(nodeExecution, null);
    }

    default void fillNodeExecutionOutput(NodeExecution nodeExecution, String targetNodeKey) {
        fillNodeExecutionOutput(nodeExecution, 0, Integer.MAX_VALUE, targetNodeKey);
    }

    default void fillNodeExecutionOutput(NodeExecution nodeExecution, int page, int size, String targetNodeKey) {
        if (nodeExecution.getFlowStartTime() == null) {
            return;
        }
        File flwDir = nodeExecution.getContextDataPath();
        String prefix = String.join("~", nodeExecution.getNodeExecutionId(), OUTPUT_FILE_SUFFIX);
        File[] outFiles = flwDir.listFiles(f -> f.getName().startsWith(prefix));
        if (outFiles == null || ArrayUtils.isEmpty(outFiles)) {
            return;
        }
        Arrays.sort(outFiles, Comparator.comparing(File::getName));
        List<NodeOutput> outputs = nodeExecution.getOutputs();
        if (outputs == null) {
            logger.warn("output is empty, but exist context file, {},{}", nodeExecution, Arrays.toString(outFiles));
            return;
        }
        int i = 0;
        for (File outFile : outFiles) {
            NodeOutput output = outputs.get(i);
            i++;
            if (targetNodeKey != null && !output.containNextNodeKey(targetNodeKey)) {
                continue;
            }
            if (page > 0) {
                page--;
            }
            try (BufferedReader br = new BufferedReader(new InputStreamReader(new GZIPInputStream(new FileInputStream(outFile)), StandardCharsets.UTF_8))) {
                String line = null;
                int count = 0;
                int start = page * size;
                int end = (page + 1) * size;
                int total = 0;
                while ((line = br.readLine()) != null) {
                    if (line.startsWith("#total")) {
                        line = line.replace("#total=", "");
                        total = Integer.parseInt(line);
                        continue;
                    }
                    if (line.startsWith("#resource=")) {
                        line = line.replace("#resource=~~", "");
                        String[] ss = line.split("~~");
                        for (String s : ss) {
                            String[] kv = s.split("@@@");
                            List<ExecutionResource> executionResources = JSONUtils.toList(kv[1], ExecutionResource.class);
                            for (ExecutionResource resource : executionResources) {
                                output.addResource(Integer.parseInt(kv[0]), resource);
                            }
                        }//end for
                        continue;
                    }
                    //total++;
                    if (count < start) {
                        count++;
                        continue;
                    }
                    if (start >= end) {
                        //stop read
                        break;
                    }
                    Map<String, Object> item = JSONUtils.toMap(line);
                    start++;
                    count++;
                    output.addResult(item);
                }//end while
                output.setTotal(total);
                logger.info("read context data: {},page:{}, nodeKey: {}, file:{}", output.log(), page, targetNodeKey, outFile.getAbsolutePath());
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }//end for
    }

    static List<ExecutionResource> saveExecutionResources(List<ExecutionResource> executionResources, File flowExecutionPath) {
        if (CollectionUtils.isEmpty(executionResources)) {
            return Collections.emptyList();
        }
        File fileDir = new File(flowExecutionPath, DIR_ATTACHMENT);
        if (!fileDir.exists()) {
            fileDir.mkdirs();
        }
        List<ExecutionResource> outResources = new ArrayList<>();
        for (ExecutionResource resource : executionResources) {
            if (!resource.isExecutionCache()) {
                outResources.add(resource);
                continue;
            }
            File resourceFile = new File(fileDir, resource.getResourceName());
            InputStreamSource streamSource = resource.buildInputStreamSource();
            if (!resourceFile.exists() && streamSource != null) {
                try (FileOutputStream fos = new FileOutputStream(resourceFile); InputStream is = streamSource.getInputStream()) {
                    IOUtils.copy(is, fos);
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            ExecutionResource executionResource = ExecutionResource.buildResource(resourceFile, false);
            outResources.add(executionResource);
        }//end for

        return outResources;
    }

    default void saveInputItems(NodeExecution nodeExecution) {
        if (nodeExecution.getFlowStartTime() == null) {
            return;
        }
        if (CollectionUtils.isNotEmpty(nodeExecution.getInputs())) {
            File flwDir = nodeExecution.getContextDataPath();
            int count = 0;
            for (ExecutionInput input : nodeExecution.getInputs()) {
                File outFile = new File(flwDir, String.join("~",
                        nodeExecution.getNodeExecutionId(), INPUT_FILE_SUFFIX + count + FILE_MIME_TYPE));
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile)), StandardCharsets.UTF_8))) {
                    for (Map<String, Object> item : input.getData()) {
                        //System.out.println(JSONUtils.toJsonString(item));
                        bw.write(JSONUtils.toJsonString(item));
                        bw.newLine();
                    }
                    bw.flush();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                count++;
            }//end for
        }
    }

    default void saveOutputItems(NodeExecution nodeExecution) {
        if (CollectionUtils.isNotEmpty(nodeExecution.getOutputs())) {
            File flwDir = nodeExecution.getContextDataPath();
            int count = 0;
            for (NodeOutput nodeOutput : nodeExecution.getOutputs()) {
                File outFile = new File(flwDir, String.join("~",
                        nodeExecution.getNodeExecutionId(), OUTPUT_FILE_SUFFIX + count + FILE_MIME_TYPE));
                int outputCount = 0;
                try (BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(new GZIPOutputStream(new FileOutputStream(outFile)), StandardCharsets.UTF_8))) {
                    bw.write("#total=" + nodeOutput.getTotal());
                    bw.newLine();
                    if (nodeOutput.getResources() != null) {
                        String rLine = "";
                        for (Map.Entry<Integer, List<ExecutionResource>> entry : nodeOutput.getResources().entrySet()) {
                            List<ExecutionResource> resources = entry.getValue();
                            List<ExecutionResource> outLists = saveExecutionResources(resources, flwDir);
                            resources.clear();
                            resources.addAll(outLists);
                            rLine += "~~" + entry.getKey() + "@@@" + JSONUtils.toJsonString(resources);
                        }
                        bw.write("#resource=" + rLine);
                        bw.newLine();
                    }
                    for (Map<String, Object> item : nodeOutput.getResults()) {
                        //System.out.println(item + "---" + JSONUtils.toJsonString(item));
                        bw.write(JSONUtils.toJsonString(item));
                        bw.newLine();
                        outputCount++;
                    }
                    bw.flush();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
                logger.info("saved outputContext data, outSize :{}, file:{}", outputCount, outFile.getAbsolutePath());
                count++;


            }//end for output
        }
    }

    static File buildFlowExecutionDataPath(String storePath, String flowKey, LocalDateTime startTime, String flowExecutionId) {
        String flwPath = String.join(File.separator, storePath, flowKey, DateTimeUtils.formatLocalDateTime(startTime, "yyyyMMddHH"), flowExecutionId);
        File flwDir = new File(flwPath);
        if (!flwDir.exists()) {
            flwDir.mkdirs();
        }
        return flwDir;
    }

    static void fillExecutionDataPath(NodeExecution nodeExecution, String storePath) {
        nodeExecution.setContextDataPath(buildFlowExecutionDataPath(storePath, nodeExecution.flowKey(), nodeExecution.getFlowStartTime(), nodeExecution.getFlowExecutionId()));
    }

}
