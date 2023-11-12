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

package io.innospots.workflow.core.execution.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.constant.PathConstant;
import io.innospots.workflow.core.utils.WorkflowUtils;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.InputStreamSource;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/1/21
 */
@Slf4j
@Getter
@Setter
@Schema(title = "execution attachment file resources")
public class ExecutionResource {

    /**
     * unique key
     */
    @Schema(title = "attachment file unique key")
    private String resourceId;

    /**
     * file name
     */
    @Schema(title = "file name")
    private String resourceName;

    private String pathKey;

    @Schema(title = "remote web uri")
    private String resourceUri;

    @Schema(title = "file local path")
    @JsonIgnore
    private String localUri;

    @Schema(title = "upload file mime type")
    private String mimeType;

    private String fileSize;

    private boolean executionCache;

    @JsonIgnore
    private InputStreamSource inputStreamSource;

    public InputStreamSource buildInputStreamSource() {
        if (inputStreamSource == null && localUri != null) {
            inputStreamSource = new FileSystemResource(localUri);
        }
        return inputStreamSource;
    }

    public Map<String,Object> toMetaInfo(){
        Map<String,Object> meta = new HashMap<>();
        meta.put("name",this.resourceName);
        meta.put("mineType",this.mimeType);
        meta.put("fileSize",this.fileSize);
        meta.put("resourceId",this.resourceId);
        meta.put("uri","file://"+resourceId);
        return meta;
    }

    public static ExecutionResource buildResource(File localFile, boolean executionCache) {
        ExecutionResource resource = new ExecutionResource();

        resource.resourceName = localFile.getName();
        double s = localFile.length();
        if(s > 1024 * 1024 *1024){
            resource.fileSize = String.format("%.2f GB",s / (1024*1024*1024d));
        }else if(s > 1024 * 1024){
            resource.fileSize = String.format("%.2f MB",s / (1024*1024d));
        }else if(s > 1024){
            resource.fileSize = String.format("%.2f KB",s / 1024d);
        }

        resource.executionCache = executionCache;
        try {
            resource.mimeType = Files.probeContentType(localFile.toPath());
        } catch (IOException e) {
        }
        if(resource.mimeType == null){
            resource.mimeType = URLConnection.getFileNameMap().getContentTypeFor(localFile.getName());
        }
        if(WorkflowUtils.encryptor!=null){
            resource.resourceId = WorkflowUtils.encryptor.encode(localFile.getAbsolutePath());
        }

        resource.resourceUri = PathConstant.ROOT_PATH +"workflow/flow-execution/resources?resourceId="+resource.resourceId;
        resource.localUri = localFile.getAbsolutePath();
        if(log.isDebugEnabled()){
            log.debug("resource info:{}",resource);
        }
        return resource;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("resourceId='").append(resourceId).append('\'');
        sb.append(", resourceName='").append(resourceName).append('\'');
        sb.append(", pathKey='").append(pathKey).append('\'');
        sb.append(", resourceUri='").append(resourceUri).append('\'');
        sb.append(", localUri='").append(localUri).append('\'');
        sb.append(", mimeType='").append(mimeType).append('\'');
        sb.append(", executionCache=").append(executionCache);
        sb.append('}');
        return sb.toString();
    }
}
