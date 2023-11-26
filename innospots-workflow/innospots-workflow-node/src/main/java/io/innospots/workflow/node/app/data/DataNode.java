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

package io.innospots.workflow.node.app.data;

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.operator.IDataOperator;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class DataNode extends BaseDataNode {

    protected IDataOperator dataOperator;

    public static final String FIELD_CREDENTIAL_KEY = "credential_key";

    protected String credentialKey;


    @Override
    protected void initialize() {
        super.initialize();
        credentialKey = valueString(FIELD_CREDENTIAL_KEY);
        IDataConnectionMinder connectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        dataOperator = connectionMinder.buildOperator();
//        dataOperator = BeanContextAwareUtils.getBean(DataOperatorManager.class).buildDataOperator(credentialKey);
    }

}
