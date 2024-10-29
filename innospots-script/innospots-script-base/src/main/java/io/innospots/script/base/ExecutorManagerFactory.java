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

package io.innospots.script.base;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import io.innospots.base.exception.ScriptException;

import java.util.concurrent.TimeUnit;

/**
 * @author Raydian
 * @date 2020/12/31
 */
public class ExecutorManagerFactory {


    private static Cache<String, ScriptExecutorManager> executorManagerCache =
            Caffeine.newBuilder()
                    .expireAfterAccess(3, TimeUnit.HOURS)
                    .build();

    public static void clear(String identifier) {
        executorManagerCache.invalidate(identifier);
    }

    public static ScriptExecutorManager getCache(String identifier){
        return executorManagerCache.getIfPresent(identifier);
    }

    public static ScriptExecutorManager getInstance(String identifier) throws ScriptException {
        ScriptExecutorManager manager =  executorManagerCache.getIfPresent(identifier);
        if (manager == null) {
            manager = ScriptExecutorManager.newInstance(identifier);
//            engine.reload();
            executorManagerCache.put(identifier, manager);
        }
        return manager;
    }

}
