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

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IQueueConnectionMinder;
import io.innospots.base.data.operator.IQueueReceiver;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.data.body.DataBody;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.TriggerNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * 消息队列触发节点
 *
 * @author Smars
 * @date 2021/4/24
 */
@Slf4j
public class QueueTriggerNode extends TriggerNode {

    public static final String FIELD_CREDENTIAL_KEY = "credentialKey";
    //    public static final String FIELD_REGISTRY_ID = "registry_id";
    public static final String FIELD_TOPIC = "topic";
    public static final String FIELD_GROUP_ID = "group_id";
    public static final String FIELD_MSG_FORMAT = "msg_format";
    public static final String FIELD_PARALLEL_NUM = "parallel_num";
    public static final String FIELD_DATA_OFFSET = "data_offset";
    public static final String FIELD_DATA_FIELDS = "data_fields";


    private IQueueReceiver dataReceiver;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        validFieldConfig(nodeInstance, FIELD_CREDENTIAL_KEY);

        validFieldConfig(nodeInstance,FIELD_TOPIC);
        validFieldConfig(nodeInstance, FIELD_GROUP_ID);

        String credentialKey = nodeInstance.valueString(FIELD_CREDENTIAL_KEY);
        String topic = nodeInstance.valueString(FIELD_TOPIC);
        String groupId = nodeInstance.valueString(FIELD_GROUP_ID);
        String messageFormat = nodeInstance.valueString(FIELD_MSG_FORMAT);

        if (topic == null) {
            throw ConfigException.buildMissingException(this.getClass(), "nodeKey: " + nodeKey() + " , topic is missing.");
        }

        Integer parallelNum = nodeInstance.valueInteger(FIELD_PARALLEL_NUM);
        if (parallelNum == null) {
            parallelNum = 1;
        }
        String dataOffset = nodeInstance.valueString(FIELD_DATA_OFFSET);
        if (dataOffset == null) {
            dataOffset = "latest";
        }

        eventBody.put(FIELD_CREDENTIAL_KEY, credentialKey);
        eventBody.put(FIELD_TOPIC, topic);
        eventBody.put(FIELD_GROUP_ID, groupId);
        eventBody.put(FIELD_MSG_FORMAT, messageFormat);
        eventBody.put(FIELD_PARALLEL_NUM, parallelNum);
        eventBody.put(FIELD_DATA_OFFSET, dataOffset);

//        DataConnectionMinderManager minderManager = ApplicationContextUtils.getBean(DataConnectionMinderManager.class);
        IQueueConnectionMinder queueConnectionMinder = (IQueueConnectionMinder) DataConnectionMinderManager.getCredentialMinder(credentialKey);
        dataReceiver = queueConnectionMinder.queueReceiver(topic,groupId,dataOffset,messageFormat,2000,10);
  //              minderManager.getMinder(credentialId);

        //dataReceiver = connectionMinder.dataReceiver(topic,groupId,dataOffset,messageFormat,2000,1);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        DataBody<Map<String, Object>> response = dataReceiver.receive();
        if (log.isDebugEnabled()) {
            log.debug("queue receive:{}", response.getBody());
        }
        if (response.getBody() == null) {
            return;
        }
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeOutput.addResult(response.getBody());
        nodeExecution.addOutput(nodeOutput);
    }


    public String topic() {
        return (String) eventBody.get(FIELD_TOPIC);
    }

    public String topicGroup() {
        return (String) eventBody.get(FIELD_GROUP_ID);
    }

    public String credentialKey() {
        return (String) eventBody.get(FIELD_CREDENTIAL_KEY);
    }

    public String messageFormat() {
        return (String) eventBody.get(FIELD_MSG_FORMAT);
    }

    public Integer parallelNumber() {
        return (Integer) eventBody.get(FIELD_PARALLEL_NUM);
    }

    public String dataOffset() {
        return (String) eventBody.get(FIELD_DATA_OFFSET);
    }
}
