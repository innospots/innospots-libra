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

package io.innospots.connector.core.data;


import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.IExecutionOperator;
import io.innospots.base.data.operator.IQueueSender;
import io.innospots.connector.core.jdbc.JdbcDataConnectionMinder;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.connector.core.minder.IDataConnectionMinder;
import io.innospots.connector.core.minder.IQueueConnectionMinder;

/**
 * @author Smars
 * @date 2021/5/3
 */
public class DataOperatorManager {

    private final DataConnectionMinderManager dataConnectionMinderManager;

    public DataOperatorManager(DataConnectionMinderManager dataConnectionMinderManager) {
        this.dataConnectionMinderManager = dataConnectionMinderManager;
    }

    public IExecutionOperator buildExecutionOperator(String credentialKey, String connectorName){
        if(credentialKey!=null){
            return buildExecutionOperator(credentialKey);
        }
        /*
        if("Http".equalsIgnoreCase(connectorName)){
            return HttpDataConnectionMinder.DEFAULT_HTTP_CONNECTION_MINDER.buildExecutionOperator();
        }
         */
        return null;
    }
    public IExecutionOperator buildExecutionOperator(String credentialKey){
        IDataConnectionMinder dataConnectionMinder = dataConnectionMinderManager.getMinder(credentialKey);
        Object dataOperator = dataConnectionMinder.buildOperator();
        if(dataOperator instanceof IExecutionOperator){
            return (IExecutionOperator) dataOperator;
        }
        return null;
    }

    public IDataConnectionMinder dataConnectionMinder(String credentialKey){
        return dataConnectionMinderManager.getMinder(credentialKey);
    }

    public IDataOperator buildDataOperator(String credentialKey) {
        JdbcDataConnectionMinder dataConnectionMinder = (JdbcDataConnectionMinder) dataConnectionMinderManager.getMinder(credentialKey);
        return dataConnectionMinder.buildOperator();
    }


    public IQueueSender buildDataSender(String credentialKey) {

        IQueueConnectionMinder queueConnectionMinder = dataConnectionMinderManager.getQueueMinder(credentialKey);
        IQueueSender queueSender = null;
        if(queueConnectionMinder!=null){
            queueSender = queueConnectionMinder.queueSender();
        }
        return queueSender;
    }
}
