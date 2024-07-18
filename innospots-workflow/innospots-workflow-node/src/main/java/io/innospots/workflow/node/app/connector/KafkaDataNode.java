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

package io.innospots.workflow.node.app.connector;

import io.innospots.base.condition.Factor;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.connector.minder.IQueueConnectionMinder;
import io.innospots.base.data.operator.IQueueSender;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class KafkaDataNode extends DataNode {

    public static final String FIELD_REGISTRY_TOPIC = "registry_topic";
    public static final String FIELD_CONFIG_FIELD = "config_field";
    public static final String FIELD_COLUMN_MAPPING = "column_mapping";
    private IQueueSender dataQueueSender;
    private SchemaRegistry schemaRegistry;

    /**
     * the columns when insert or update operation
     */
    protected List<Factor> columnFields;
    protected boolean hasConfigField;
    private String topic;

    @Override
    protected void initialize() {
        //this.fillNodeInfo(nodeInstance);
        credentialKey = valueString(FIELD_CREDENTIAL_KEY);
        topic = valueString(FIELD_REGISTRY_TOPIC);
        hasConfigField = valueBoolean(FIELD_CONFIG_FIELD);
        if(hasConfigField){
            validFieldConfig(FIELD_COLUMN_MAPPING);
            List<Map<String, Object>> columnFieldMapping = valueMapList(FIELD_COLUMN_MAPPING);
            columnFields = BeanUtils.toBean(columnFieldMapping, Factor.class);
        }

        IDataConnectionMinder connectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        if(connectionMinder instanceof IQueueConnectionMinder){
            dataQueueSender = ((IQueueConnectionMinder) connectionMinder).queueSender();
        }
        /*
        CachedSchemaRegistryReader dataSchemaReader = ApplicationContextUtils.getBean(CachedSchemaRegistryReader.class);
        schemaRegistry = dataSchemaReader.getSchemaRegistry(credentialId, tableName, null);
        if (schemaRegistry == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "the topic registry is not exist, credentialId: " + credentialId + " , registryCode: " + this.tableName);
        }
        topic = (String) Optional.ofNullable(schemaRegistry.getConfigs().get("topic")).orElse(null);
         */

        if (topic == null) {
            throw ConfigException.buildMissingException(this.getClass(), "credentialId: " + credentialKey  + " , topic is null");
        }
    }

    /*
    @Override
    protected void invoke(NodeExecution nodeExecution, FlowExecution flowExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys);
        nodeExecution.addOutput(nodeOutput);
        StringBuilder msg = new StringBuilder();
        try{
            List<Map<String, Object>> sendData = new ArrayList<>();
            for (Map<String, Object> item : flowExecution.getContexts()) {
                if(CollectionUtils.isNotEmpty(this.columnFields)){
                    Map<String,Object> data = new HashMap<>(this.columnFields.size());
                    for (Factor columnField : this.columnFields) {
                        data.put(columnField.getCode(),columnField.value(item));
                    }
                    sendData.add(data);
                }else{
                    sendData.add(item);
                }

            }//end for
            InnospotResponse<Map<String,Object>> response = dataSenderPoint.sendBatch(datasourceId,topic,sendData);
            if(response.hasData()){
                fillOutput(nodeOutput,response.getBody());
            }
            msg.append(response.getMessage());
        }catch (Exception e){
            log.error(e.getMessage());
            msg.append(e.getMessage());
            msg.append("\n");
        }
        nodeExecution.setMessage(msg.toString());
    }

     */


    @Override
    public void invoke(NodeExecution nodeExecution) {
        NodeOutput nodeOutput = new NodeOutput();
        nodeOutput.addNextKey(this.nextNodeKeys());
        nodeExecution.addOutput(nodeOutput);
        List<Map<String, Object>> sendData = new ArrayList<>();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                if(this.columnFields!=null){
                    Map<String, Object> data = new HashMap<>(this.columnFields.size());
                    for (Factor columnField : this.columnFields) {
                        data.put(columnField.getCode(), item.get(columnField.getCode()));
                    }
                    fillOutput(nodeOutput, data);
                    sendData.add(data);
                }else{
                    Map<String, Object> data = new HashMap<>(item);
                    fillOutput(nodeOutput,data);
                    sendData.add(data);
                }
            }//end for item
        }//end for execution input
        Map<String,Object> resp = dataQueueSender.send(topic,sendData);
        //InnospotResponse<Map<String, Object>> response = dataSenderPoint.sendBatch(credentialId, topic, sendData);
        nodeExecution.setMessage(resp.toString());
    }
}
