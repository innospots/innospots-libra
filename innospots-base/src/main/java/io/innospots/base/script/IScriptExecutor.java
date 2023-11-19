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

package io.innospots.base.script;

import io.innospots.base.model.Pair;
import io.innospots.base.script.jit.MethodBody;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/31
 */
public interface IScriptExecutor {

    void initialize(Method method);

    ExecuteMode executeMode();

    String scriptType();

    String suffix();

    Object execute(Map<String, Object> env);

    default Object execute(Object... args){
        Map<String, Object> env = new HashMap<>();
        if(arguments()==null){
            return execute(env);
        }
        for (int i = 0; i < args.length; i++) {
            env.put(arguments()[i], args[i]);
        }
        return execute(env);
    }

    String[] arguments();


    default void reBuildMethodBody(MethodBody methodBody){
    }

    default boolean executeBoolean(Map<String,Object> env){
        Object v = execute(env);
        boolean o = false;
        if (v instanceof String) {
            o = Boolean.parseBoolean((String) v);
        }
        if (v instanceof Boolean) {
            o = (Boolean) v;
        }//end if
        return o;
    }

    default Pair<Class<?>,String>[] argsPair(String[] args) throws ClassNotFoundException {
        Pair<Class<?>,String>[] pairs = new Pair[args.length];
        for (int i = 0; i < args.length; i++) {
            String[] ss= args[i].split(" ");
            if (ss.length == 2) {
                pairs[i] = Pair.of(Class.forName(ss[0]),ss[1]);
            }
        }
        return pairs;
    }

}
