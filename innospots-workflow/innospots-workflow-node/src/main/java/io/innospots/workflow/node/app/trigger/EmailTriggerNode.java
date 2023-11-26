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
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.data.body.DataBody;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;

import java.util.Map;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/12
 */
public class EmailTriggerNode extends CycleTimerNode {


    public static final String FIELD_CREDENTIAL_KEY = "credential_key";
    public static final String FIELD_MAIL_BOX = "mail_folder";

    public static final String FIELD_ACTION = "action";
    public static final String FIELD_HAS_ATTACH = "has_attach";
    public static final String FIELD_ATTACH_PREFIX = "attach_prefix";

    private String credentialKey;

    private String mailBoxName;

    private String actionName;

    private boolean hasAttachments;

    private String attachPrefix;

    private ConnectionCredential connectionCredential;

    private IExecutionOperator executionOperator;

    @Override
    protected void initialize() {
        credentialKey = validString(FIELD_CREDENTIAL_KEY);
        mailBoxName = validString(FIELD_MAIL_BOX);
        actionName = valueString(FIELD_ACTION);
        hasAttachments = valueBoolean(FIELD_HAS_ATTACH);

        if (hasAttachments) {
            attachPrefix = valueString(FIELD_ATTACH_PREFIX);
        }
        IDataConnectionMinder connectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        executionOperator = connectionMinder.buildOperator();

    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
        ItemRequest itemRequest = new ItemRequest();
        itemRequest.add(FIELD_MAIL_BOX, mailBoxName);
        if (actionName != null) {
            itemRequest.add(FIELD_ACTION, actionName);
        }
        if (attachPrefix != null) {
            itemRequest.add(FIELD_ATTACH_PREFIX, attachPrefix);
        }
        DataBody<Map<String, Object>> dataBody = executionOperator.execute(itemRequest);

    }
}
