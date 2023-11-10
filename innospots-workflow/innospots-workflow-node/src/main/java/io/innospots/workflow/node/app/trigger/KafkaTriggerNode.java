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

package io.innospots.workflow.node.app.trigger;

import io.innospots.workflow.core.runtime.webhook.FlowWebhookConfig;


/**
 * webhook作为触发节点，没有上一级节点
 * 节点包含异步调用，同步调用
 * 以及可以配置是否返回调用结果返回内容
 *
 * @author Smars
 * @date 2021/4/24
 */
public class KafkaTriggerNode extends QueueTriggerNode {

    public static final String FIELD_API_TRIGGER_ID = "kafka_trigger_id";
    private FlowWebhookConfig flowWebhookConfig;


    /**
     * 是否为异步调用，true为异步调用
     */
    public static final String FIELD_ASYNC = "call_async";

}
