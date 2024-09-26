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

package io.innospots.workflow.core.instance.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.node.NodeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;

import jakarta.validation.constraints.NotNull;
import java.util.*;

import static java.lang.Boolean.valueOf;
import static java.util.Objects.hash;
import static org.slf4j.LoggerFactory.getLogger;

/**
 * @author Raydian
 * @date 2020/11/28
 */
@Getter
@Setter
public class NodeInstance extends NodeInfo {

    private static final Logger logger = getLogger(NodeInstance.class);

    @NotNull
    @Schema(title = "node definition id")
    protected Integer nodeDefinitionId;

    protected String displayName;

//    @NotEmpty
//    @Schema(title = "the class name of the node")
//    protected String nodeType;

    @Schema(title = "width")
    protected Integer width;

    @Schema(title = "height")
    protected Integer height;

    @Schema(title = "x position")
    protected Integer x;

    @Schema(title = "y position")
    protected Integer y;

    @Schema(title = "form element values")
    protected Map<String, Object> data;

    @Schema(title = "if true, the node status is pause")
    protected Boolean pauseFlag;

    @Schema(title = "node ports")
    protected List<Map<String, Object>> ports;

    @Schema(title = "nodeInstance primary key")
    protected Long nodeInstanceId;

    @Schema(title = "generate by frontend, this is unique in the flowInstance")
    protected String nodeKey;

    @Schema(title = "output field code")
    protected List<ParamField> outputFields;

    @Schema(title = "input param field codes")
    protected List<ParamField> inputFields;

    @Schema(title = "continue execute next node if fail")
    protected boolean continueOnFail;

    @Schema(title = "retry if node execute fail")
    protected boolean retryOnFail;

    @Schema(title = "max times to retry, if node execute fail")
    protected int maxTries;

    @Schema(title = "the wait time mills before retry, if node execute fail")
    protected int retryWaitTimeMills;

    @Schema(title = "async execute node")
    protected boolean async;

    @Schema(title = "if true, enable fail branch output")
    protected boolean failureBranch;

    @Schema(title = "next execute node")
    protected List<String> nextNodeKeys;

    @Schema(title = "previous nodes")
    protected List<String> prevNodeKeys;

    @Schema(title = "if true, store node execution output")
    protected boolean storeOutput;

    @Schema(title = "the edge of this node, which the source anchor point to target node. key is this node source anchor value, values list include the node keys that the anchor point to the target nodes.")
    @JsonIgnore
    protected Map<String, List<String>> nodeAnchors;

//    @Schema(title = "key is form name, value is the code editor codeType")
//    protected Map<String, ScriptType> scriptTypes;

    @Schema(title = "node color")
    protected String color;

    private Long workflowInstanceId;

    @Schema(title = "timeout millisecond")
    protected int timeoutMills=60000;

    /*
    public void addScriptType(String name, ScriptType type) {
        if (this.scriptTypes == null) {
            this.scriptTypes = new HashMap<String, ScriptType>();
        }
        this.scriptTypes.put(name, type);
    }

     */

    public String expName() {
        return "$" + nodeKey;
    }

    public String scriptName(String name) {
        return expName() + "_" + name;
    }


    public List<String> findNodeKeysByAnchor(String anchorKey) {
        if (this.nodeAnchors != null) {
            return nodeAnchors.get(anchorKey);
        }
        return Collections.emptyList();
    }

    public String simpleInfo() {
        return "[code:" + code + ";name:" + this.displayName + ";nodeKey:" + this.nodeKey + "]";
    }


    public boolean containsKey(String key) {
        if (data == null) {
            return false;
        }
        return data.containsKey(key);
    }

    public Object value(String key) {
        if (data == null) {
            return null;
        }
        return data.get(key);
    }

    public List<Map<String, Object>> valueList(String key) {
        Object v = data.get(key);
        if (v instanceof List) {
            return (List<Map<String, Object>>) v;
        }
        return null;
    }

    public Map<String, Object> valueMap(String key) {
        Object m = data.get(key);
        if (m instanceof Map) {
            return (Map<String, Object>) m;
        }
        return null;
    }

    public Boolean valueBoolean(String key) {
        return valueOf(valueString(key));
    }

    public String valueString(String key) {
        Object o = value(key);
        if (o instanceof String) {
            return (String) o;
        }
        if (o instanceof Collection){
            return StringUtils.join((Collection)o,",");
        }
        if (o == null) {
            return null;
        } else {
            return String.valueOf(o);
        }
    }

    public Double valueDouble(String key){
        Object o = value(key);
        if (o instanceof Double) {
            return (Double) o;
        }
        if (o == null) {
            return null;
        } else {
            return Double.valueOf(o.toString());
        }
    }

    public Long valueLong(String key) {
        Object o = value(key);
        if (o instanceof Long) {
            return (Long) o;
        }
        if (o == null) {
            return null;
        } else {
            return Long.valueOf(o.toString());
        }
    }

    public Integer valueInteger(String key) {
        Object o = value(key);
        if (o instanceof Integer) {
            return (Integer) o;
        }
        if (o == null) {
            return null;
        } else {
            return Integer.valueOf(o.toString());
        }
    }

    /*
    public ScriptType getActionScriptType() {
        String v = this.valueString(FIELD_ACTION_SCRIPT_TYPE);
        if (v != null) {
            try {
                return ScriptType.valueOf(v);
            } catch (Exception e) {
                return ScriptType.JAVA;
            }

        }
        return ScriptType.JAVA;
    }

    public String getAction() {
        return this.valueString(FIELD_ACTION_SCRIPT);
    }

    public Map<String, String> getFunctions() {
        Object m = this.value(FIELD_FUNCTIONS);
        if (m instanceof Map) {
            return (Map<String, String>) m;
        }
        return null;
    }

     */

    /**
     * 多个表达式处理函数
     *
     * @return
     */

    /*
    public List<MethodBody> expMethods() {
        List<MethodBody> methodBodyList = new ArrayList<>();
        if (MapUtils.isEmpty(this.scriptTypes)) {
            return methodBodyList;
        }
        for (Map.Entry<String, ScriptType> scriptTypeEntry : this.scriptTypes.entrySet()) {
            String script = this.valueString(scriptTypeEntry.getKey());
            if (StringUtils.isEmpty(script)) {
                continue;
            }
            MethodBody mb = new MethodBody(scriptTypeEntry.getValue());
            mb.setReturnType(Object.class);
            mb.setFormName(scriptTypeEntry.getKey());
            mb.setMethodName(this.scriptName(scriptTypeEntry.getKey()));
            mb.setSrcBody(script);
            methodBodyList.add(mb);
        }
        return methodBodyList;
    }

     */


    /**
     * node expression source
     *
     * @return
     */
    /*
    public MethodBody expSource() {
        try {
            String action = this.getAction();
            if (action == null) {
                return null;
            }

            MethodBody methodBody = new MethodBody();
            methodBody.setReturnType(Object.class);
            methodBody.setScriptType(this.getActionScriptType());
            if (CollectionUtils.isNotEmpty(this.inputFields)) {
                methodBody.setParams(this.getInputFields());
            }
            methodBody.setMethodName(this.expName());
            String src = action;
            //src = SourceTemplateUtils.output(this.nodeType, action, this.getData());
            methodBody.setSrcBody(src);
            return methodBody;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
    */

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("nodeInstanceId=").append(nodeInstanceId);
        sb.append(", nodeDefinitionId=").append(nodeDefinitionId);
        sb.append(", x=").append(x);
        sb.append(", y=").append(y);
        sb.append(", nodeKey='").append(nodeKey).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", nodeType='").append(nodeType).append('\'');
        sb.append('}');
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof NodeInstance)) {
            return false;
        }
        NodeInstance that = (NodeInstance) o;
        return Objects.equals(nodeDefinitionId, that.nodeDefinitionId) &&
                Objects.equals(nodeType, that.nodeType) &&
                Objects.equals(x, that.x) && Objects.equals(y, that.y) &&
                Objects.equals(data, that.data) &&
                Objects.equals(nodeKey, that.nodeKey) &&
//                Objects.equals(this.actionScriptType, that.actionScriptType) &&
                Objects.equals(this.continueOnFail, that.continueOnFail) &&
//                Objects.equals(this.inputFields, that.inputFields) &&
                Objects.equals(this.outputFields, that.outputFields) &&
                Objects.equals(this.maxTries, that.maxTries) &&
                Objects.equals(this.nextNodeKeys, that.nextNodeKeys) &&
                Objects.equals(this.prevNodeKeys, that.prevNodeKeys) &&
                Objects.equals(this.retryWaitTimeMills, that.retryWaitTimeMills) &&
                Objects.equals(this.retryOnFail, that.retryOnFail) &&
                Objects.equals(this.code, that.code) &&
                Objects.equals(this.color, that.color) &&
                Objects.equals(this.description, that.description) &&
                Objects.equals(this.displayName, that.displayName) &&
                Objects.equals(this.icon, that.icon) &&
                Objects.equals(this.name, that.name) &&
                Objects.equals(pauseFlag, that.pauseFlag);
    }

    @Override
    public int hashCode() {
        return hash(nodeDefinitionId, nodeType, x, y, data,
                nodeKey, continueOnFail, outputFields,
                maxTries, nextNodeKeys, prevNodeKeys, retryWaitTimeMills, retryOnFail,
                code, color, description, displayName, icon, name, pauseFlag);
    }
}
