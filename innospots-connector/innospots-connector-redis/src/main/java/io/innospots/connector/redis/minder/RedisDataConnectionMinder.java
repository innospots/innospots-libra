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

package io.innospots.connector.redis.minder;

import io.innospots.base.data.minder.BaseDataConnectionMinder;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.connector.redis.operator.RedisDataOperator;
import redis.clients.jedis.Jedis;

/**
 * @author Alfred
 * @date 2022/10/22
 */
public class RedisDataConnectionMinder extends BaseDataConnectionMinder {

    private static final String SERVER_IP = "server_ip";
    private static final String PORT = "port";

    private Jedis jedis;
    private RedisDataOperator dataOperator;

    @Override
    public void open() {
        if (this.jedis != null) {
            return;
        }

        String serverIp = String.valueOf(this.connectionCredential.getConfig().get(SERVER_IP));
        int port = Integer.parseInt(this.connectionCredential.getConfig().get(PORT).toString());
        this.jedis = new Jedis(serverIp, port);
    }

    @Override
    public Object test(ConnectionCredential connectionCredential) {
        String serverIp = connectionCredential.v(SERVER_IP);
        int port = Integer.parseInt(connectionCredential.v(PORT));
        Jedis jedis = new Jedis(serverIp, port);
        jedis.ping();
        jedis.close();
        return true;
    }

    @Override
    public void close() {
        this.jedis.close();
    }


    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return this.jedis.get(tableName);
    }


    @Override
    public IOperator buildOperator() {
        return new RedisDataOperator(this.jedis);
    }
}
