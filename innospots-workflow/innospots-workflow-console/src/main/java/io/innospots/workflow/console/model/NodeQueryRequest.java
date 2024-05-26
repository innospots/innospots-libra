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

package io.innospots.workflow.console.model;

import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.bind.annotation.RequestParam;

@Getter
@Setter
@Schema(title = "node definition query request")
public class NodeQueryRequest {

    private DataStatus dataStatus;

    private Integer flowTplId = 1;

    protected Integer nodeGroupId;

    @Schema(title = "query input something")
    protected String queryInput;

    @Schema(title = "the current number of pages")
    protected int page = 1;

    @Schema(title = "number of entries per page")
    protected int size = 20;

    protected NodePrimitive primitive;

}
