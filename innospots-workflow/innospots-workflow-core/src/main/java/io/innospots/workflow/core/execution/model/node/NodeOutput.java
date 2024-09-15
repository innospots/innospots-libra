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

import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.FieldValueTypeConverter;
import io.innospots.base.model.field.ParamField;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import java.util.*;
import java.util.stream.Collectors;

/**
 * node execute output for each branch
 *
 * @author Smars
 * @version 1.0.0
 * @date 2022/2/4
 */
@Getter
@Setter
public class NodeOutput {

    private List<Map<String, Object>> results = new ArrayList<>();

    /**
     * key : item position
     */
    private Map<Integer, List<ExecutionResource>> resources;

    private Set<String> nextNodeKeys = new HashSet<>();

    private String name;

    private long total;

    private Map<String, Object> logs = new LinkedHashMap<>();

    public NodeOutput() {
    }

    public NodeOutput(String name) {
        this.name = name;
    }

    public boolean containNextNodeKey(String nodeKey) {
        return nextNodeKeys.contains(nodeKey);
    }

    public void addResult(Map<String, Object> item) {
        results.add(item);
    }

    public void addResult(String key, Object value) {
        Map<String, Object> r = new LinkedHashMap<>();
        r.put(key, value);
        this.addResult(r);
    }

    public void addResult(Collection<Map<String, Object>> items) {
        results.addAll(items);
    }

    public void fillTotal() {
        total = results.size();
    }

    public List<ExecutionResource> itemResources(Integer position) {
        if (resources != null && position < resources.size()) {
            return this.resources.get(position);
        }
        return Collections.emptyList();
    }

    public void addResource(Integer position, ExecutionResource executionResource) {
        if (resources == null) {
            this.resources = new LinkedHashMap<>();
        }
        List<ExecutionResource> executionResources = null;
        if (this.resources.containsKey(position)) {
            executionResources = this.resources.get(position);
        } else {
            executionResources = new ArrayList<>();
            this.resources.put(position, executionResources);
        }
        executionResources.add(executionResource);
    }

    public void addNextKey(String nodeKey) {
        if (nodeKey != null) {
            this.nextNodeKeys.add(nodeKey);
        }
    }

    public void addNextKey(Collection<String> nodeKeys) {
        if (CollectionUtils.isNotEmpty(nodeKeys)) {
            this.nextNodeKeys.addAll(nodeKeys);
        }
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", nextNodeKeys=").append(nextNodeKeys);
        sb.append(", resources=").append(resources);
        sb.append(", results=").append(results);

        sb.append('}');
        return sb.toString();
    }

    public NodeOutput copy() {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.resources = resources;
        nodeOutput.name = name;
        nodeOutput.nextNodeKeys = nextNodeKeys;
        nodeOutput.total = total;
        return nodeOutput;
    }

    public Map<String, Object> log() {
        return log(true);
    }

    public Map<String, Object> log(boolean detail) {
        Map<String, Object> logs = new LinkedHashMap<>();
        logs.put("items", results.size());
        logs.put("total", total);
        logs.put("nextNodeKeys", this.nextNodeKeys);
        if (!results.isEmpty() && detail) {
            logs.put("columns", results.get(0).keySet().size());
        }
        if (MapUtils.isNotEmpty(resources)) {
            logs.put("size", resources.size());
            if (detail) {
                List<Map<String, Object>> metas = new ArrayList<>();
                for (List<ExecutionResource> executionResources : resources.values()) {
                    metas.addAll(executionResources.stream().map(ExecutionResource::toMetaInfo).toList());
                }
                logs.put("resources", metas);
            }
        }
        logs.putAll(this.logs);
        return logs;
    }

    public void addLog(String key, Object value) {
        this.logs.put(key, value);
    }

    public void addLog(Map<String, Object> logData) {
        if (logData != null) {
            this.logs.putAll(logData);
        }
    }

    public int size() {
        return this.results.size();
    }


    public static List<ParamField> buildOutputField(List<Map<String, Object>> listResult) {
        List<ParamField> outputFields = new ArrayList<>();
        //all output data have the save fields
        if (listResult != null && !listResult.isEmpty()) {
            Map<String, Object> data = listResult.get(0);
            for (String key : data.keySet()) {
                Object v = data.get(key);
                ParamField pf = new ParamField(key, key, FieldValueTypeConverter.convertJavaTypeByValue(v));
                if (v instanceof Map) {
                    //the value is map object
                    pf.setSubFields(parseFieldFromValue(pf.getCode(), (Map<?, ?>) v));
                } else if (v instanceof Collection) {
                    Object obj = ((Collection<?>) v).stream().findFirst().orElse(null);
                    if (obj instanceof Map) {
                        pf.setSubFields(parseFieldFromValue(pf.getCode(), (Map<?, ?>) obj));
                    }
                }
                outputFields.add(pf);
            }

        }
        return outputFields;
    }

    private static List<ParamField> parseFieldFromValue(String parentCode, Map<?, ?> value) {
        List<ParamField> subFields = new ArrayList<>();
        for (Map.Entry<?, ?> entry : ((Map<?, ?>) value).entrySet()) {
            String k = entry.getKey().toString();
            ParamField subField = new ParamField(k, k, FieldValueTypeConverter.convertJavaTypeByValue(entry.getValue()));
            subField.setParentCode(parentCode);
            subFields.add(subField);
        }
        return subFields;
    }
}
