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

package io.innospots.data.provider;

import datart.ViewExecuteParam;
import datart.provider.StdSqlOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.*;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/10/10
 */
@Slf4j
public class SqlScriptBuilderManager {

    private static final Map<String, ISqlScriptBuilder> sqlScriptBuilders = new HashMap<>();

    static {
        loadBuilder();
    }

    private static void loadBuilder() {
        ServiceLoader<ISqlScriptBuilder> serviceLoader = ServiceLoader.load(ISqlScriptBuilder.class);

        for (ISqlScriptBuilder scriptBuilder : serviceLoader) {
            sqlScriptBuilders.put(scriptBuilder.dbType(), scriptBuilder);
            log.debug("Loading sql script builder:{}", scriptBuilder.getClass());
        }

        log.debug("Loaded sql script builder size:{}", sqlScriptBuilders.size());
    }

    public static ISqlScriptBuilder getInstance(String type) {
        return sqlScriptBuilders.get(type);
    }


    public static String buildCountSql(String dbType, ViewExecuteParam viewExecuteParam) {
        ISqlScriptBuilder scriptBuilder = getInstance(dbType);
        if (scriptBuilder == null) {
            return null;
        }
        return scriptBuilder.buildCountSql(viewExecuteParam);
    }

    public static String buildSql(String dbType, ViewExecuteParam viewExecuteParam) {
        ISqlScriptBuilder scriptBuilder = getInstance(dbType);
        if (scriptBuilder == null) {
            return null;
        }
        return scriptBuilder.buildSql(viewExecuteParam);
    }

    public static Boolean functionValidate(String dbType, String snippet) {
        ISqlScriptBuilder scriptBuilder = getInstance(dbType);
        if (scriptBuilder == null) {
            return null;
        }
        return scriptBuilder.functionValidate(snippet);
    }

    public static Set<StdSqlOperator> supportedFunctions(String dbType) {
        ISqlScriptBuilder scriptBuilder = getInstance(dbType);
        if (scriptBuilder == null) {
            return null;
        }
        return scriptBuilder.supportedStdFunctions();
    }
}
