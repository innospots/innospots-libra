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

package io.innospots.workflow.core.flow.loader;

import cn.hutool.core.io.resource.ResourceUtil;
import com.fasterxml.jackson.databind.json.JsonMapper;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/**
 * 通过文件加载流程实例
 *
 * @author Smars
 * @date 2021/4/20
 */
public class FsWorkflowLoader extends BaseWorkflowLoader {

    private static final Logger logger = LoggerFactory.getLogger(FsWorkflowLoader.class);

//    public static final String DIR_FLOW_CONFIG = "flow_conf";

    private String workflowBuildPath;

    private LoadingCache<String, WorkflowBody> flowInstanceCache;

    public FsWorkflowLoader(String workflowBuildPath) {
        this.workflowBuildPath = workflowBuildPath;
        flowInstanceCache = Caffeine.newBuilder()
                .expireAfterAccess(6, TimeUnit.MINUTES)
                .build(this::loadInstanceFormFile);
    }

    private WorkflowBody loadInstanceFormFile(String key) {
        String flowJsonFile = null;
        try {
            flowJsonFile = ResourceUtil.readUtf8Str(workflowBuildPath  + key + ".json");
        }catch (Exception e){
            logger.warn("workflowInstance config not exist, file:{}", workflowBuildPath + key + ".json");
            return null;
        }

        if (flowJsonFile == null) {
            return null;
        }
        WorkflowBody workflowBody = null;
        JsonMapper jsonMapper = new JsonMapper();
        try {
            workflowBody = jsonMapper.readValue(flowJsonFile, WorkflowBody.class);
            workflowBody.initialize();
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return workflowBody;
    }


    @Override
    public WorkflowBody loadWorkflow(Long workflowInstanceId, Integer revision) {
        return flowInstanceCache.get(IWorkflowLoader.key(workflowInstanceId, revision));
    }

    @Override
    public WorkflowBody loadWorkflow(String flowKey) {
        return flowInstanceCache.get(flowKey);
    }

}
