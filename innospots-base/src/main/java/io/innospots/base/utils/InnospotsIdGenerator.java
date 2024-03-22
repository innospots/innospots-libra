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

package io.innospots.base.utils;

import cn.hutool.core.lang.generator.SnowflakeGenerator;

/**
 * @author Smars
 * @date 2021/6/29
 */
public class InnospotsIdGenerator {

    private static final long MAX_DATACENTER_NUM = 1024L;

    private static InnospotsIdGenerator innospotsIdGenerator;
    private SnowflakeGenerator idGenerator;

    public InnospotsIdGenerator(long dataCenterId, long machineId) {
//        idGenerator = new SnowflakeGenerator(machineId,dataCenterId);
        idGenerator = new SnowflakeGenerator(0,0);
    }

    public static InnospotsIdGenerator build(String ip, int port) {
        if (innospotsIdGenerator == null) {
            innospotsIdGenerator = new InnospotsIdGenerator(port % MAX_DATACENTER_NUM,ipToLong(ip));
        }
        return innospotsIdGenerator;
    }

    private static long ipToLong(String ipAddress) {
        long result = 0;
        if(ipAddress.matches("[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}\\.[\\d]{1,3}")){
            String[] atoms = ipAddress.split("\\.");
            for (int i = 3; i >= 0; i--) {
                result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
            }
        }else{
            result = ipAddress.hashCode();
        }

        return result % MAX_DATACENTER_NUM;
    }

    public static long generateId() {
        return innospotsIdGenerator.nextId();
    }

    public static String generateIdStr() {
        return String.valueOf(generateId());
    }

    /**
     * generator id
     *
     * @return
     */
    public synchronized long nextId() {
        return idGenerator.next();
    }

}
