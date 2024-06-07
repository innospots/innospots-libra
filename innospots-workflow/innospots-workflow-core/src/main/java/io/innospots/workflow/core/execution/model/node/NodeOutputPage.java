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

package io.innospots.workflow.core.execution.model.node;

import io.innospots.base.data.body.PageBody;
import io.innospots.base.execution.ExecutionResource;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.*;

/**
 * @author Smars
 * @date 2023/8/18
 */
@Getter
@Setter
public class NodeOutputPage {

    private PageBody<Map<String, Object>> results = new PageBody<>();

    /**
     * key : item position
     */
    private Map<Integer,List<ExecutionResource>> resources;

    private Set<String> nextNodeKeys = new HashSet<>();

    private String name;

    public boolean isEmpty() {
        return CollectionUtils.isEmpty(results.getList());
    }
    public NodeOutputPage(long page,long size) {
        results.setPageSize(size);
        results.setCurrent(page);
    }

    public NodeOutputPage(NodeOutput nodeOutput,long page,long size) {
        results.setPageSize(size);
        results.setCurrent(page);
        results.setTotal(nodeOutput.getTotal());
        //results.setList(nodeOutput.getResults());
        this.resources = nodeOutput.getResources();
        this.nextNodeKeys = nodeOutput.getNextNodeKeys();
        this.name = nodeOutput.getName();
    }
    public void addItem(Map<String,Object> item){
        this.results.add(item);
    }
}
