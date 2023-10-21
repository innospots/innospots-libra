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

package io.innospots.connector.mysql.minder;


import io.innospots.base.data.minder.JdbcDataConnectionMinder;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.data.operator.ISqlOperator;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.connector.mysql.operator.MysqlDataOperator;
import io.innospots.connector.mysql.operator.MysqlSqlOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.Map;

/**
 * @author Raydian
 * @date 2021/1/31
 */
@Slf4j
public class MysqlDataConnectionMinder extends JdbcDataConnectionMinder {


    @Override
    public void open() {

        if (dataSource != null) {
            return;
        }
        Map<String, Object> config = this.connectionCredential.getConfig();
        config.put(DRIVER_CLASS_NAME, "com.mysql.cj.jdbc.Driver");

        this.dataSource = buildDataSource(connectionCredential);
    }


    @Override
    public Object test(ConnectionCredential connectionCredential) {
        connectionCredential.getConfig().put(DRIVER_CLASS_NAME, "com.mysql.cj.jdbc.Driver");
        return super.test(connectionCredential);
    }

    @Override
    public IDataOperator dataOperator() {
        if (this.dataOperator == null) {
            this.dataOperator = new MysqlDataOperator(dataSource);
        }
        return dataOperator;
    }

    @Override
    public ISqlOperator sqlOperator() {
        if (this.sqlOperator == null) {
            this.sqlOperator = new MysqlSqlOperator(dataSource);
        }
        return sqlOperator;
    }

    @Override
    public String connector() {
        return "mysql";
    }
}
