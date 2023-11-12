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

package io.innospots.workflow.core.node.executor;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @date 2021/5/8
 */
public class TriggerNode extends BaseNodeExecutor {

    protected Map<String, Object> eventBody = new HashMap<>();

    public static final String FIELD_NEXT_NODES = "next_nodes";

    @Override
    protected void initialize() {
        eventBody.put(FIELD_NEXT_NODES, ni.getNextNodeKeys());
        eventBody.put("node_key", this.nodeKey());
        eventBody.put("node_instance_id", ni.getNodeInstanceId());
        eventBody.put("node_code", ni.getCode());
    }

    @Override
    protected Object processItem(Map<String, Object> item) {
        return item;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof TriggerNode)) {
            return false;
        }
        TriggerNode that = (TriggerNode) o;
        return Objects.equals(eventBody, that.eventBody) &&
                Objects.equals(ni.getNextNodeKeys(), that.ni.getNextNodeKeys());
    }

    @Override
    public int hashCode() {
        return Objects.hash(eventBody, ni.getNextNodeKeys());
    }
}
