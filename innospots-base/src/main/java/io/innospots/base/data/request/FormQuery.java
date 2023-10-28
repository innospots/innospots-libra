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

package io.innospots.base.data.request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenc
 * @date 2021/3/11 10:10
 */
@Getter
@Setter
@Schema(title = "simple query")
public class FormQuery {

    protected Integer categoryId;

    @Schema(title = "query input something")
    protected String queryInput;

    @Schema(title = "whether the paging")
    protected Boolean paging = true;

    @Schema(title = "the current number of pages")
    protected int page = 1;

    @Schema(title = "number of entries per page")
    protected int size = 20;

    @Schema(title = "sort field")
    protected String sort;

    @Schema(title = "collation (ASC for ascending, DESC for descending)")
    protected Boolean asc = true;
}
