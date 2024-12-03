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

package io.innospots.workflow.core.enums;


import io.innospots.base.i18n.I18nUtils;
import io.innospots.base.model.field.SelectItem;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Enum class for node primitives.
 *
 * @author Smars
 * @date 2021/3/22
 */
public enum NodePrimitive {

    /**
     * create and build item date
     */
    data("workflow.node.primitive.data:数据"),

    /**
     *
     */
    api("workflow.node.primitive.api:API"),

    response("workflow.node.primitive.response:结果应答"),

    ai("workflow.node.primitive.ai:人工智能"),

    connector("workflow.node.primitive.connector:连接器"),

    file("workflow.node.primitive.file:文件"),

    dataset("workflow.node.primitive.dataset:数据集"),

    compute("workflow.node.primitive.compute:计算"),
    /**
     * Execute node.
     */
    execute("workflow.node.primitive.execute:执行器"),
    /**
     * Logic node.
     */
    logic("workflow.node.primitive.logic:逻辑"),
    /**
     * Interaction node.
     */
    interaction("workflow.node.primitive.interaction:交互"),
    /**
     * Script node.
     */
    script("workflow.node.primitive.script:脚本"),
    /**
     * Trigger node.
     */
    trigger("workflow.node.primitive.trigger:触发器"),

    scheduleTrigger("workflow.node.primitive.trigger.schedule:调度触发器"),

    apiTrigger("workflow.node.primitive.api:API触发器"),

    approve("workflow.node.primitive.approve:审批"),

    approveTrigger("workflow.node.primitive.approveTrigger:审批触发节点"),
    /**
     * Normal node.
     */
    normal("workflow.node.primitive.normal:通用");

    private String label;

    private String tips;

    NodePrimitive(String label) {
        this.label = I18nUtils.wrapKey(label);
    }


    public String label() {
        return this.label;
    }

    public boolean isTrigger(){
        return this.name().toLowerCase().endsWith(NodePrimitive.trigger.name());
    }

    public static List<SelectItem> primitiveSelectItems() {
        return Arrays.stream(NodePrimitive.values())
                .map(nodePrimitive ->
                        new SelectItem(nodePrimitive.label(), nodePrimitive.name(), nodePrimitive.tips))
                .collect(Collectors.toList());
    }
}

