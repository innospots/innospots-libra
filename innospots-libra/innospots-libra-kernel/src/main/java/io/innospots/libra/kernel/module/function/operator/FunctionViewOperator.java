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

package io.innospots.libra.kernel.module.function.operator;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.innospots.script.function.definition.model.FunctionDefinition;
import io.innospots.script.function.definition.operator.FunctionDefinitionOperator;
import io.innospots.libra.kernel.module.function.model.FunctionCategory;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * 函数选择列表
 *
 * @author Smars
 * @date 2021/8/29
 */
@Service
public class FunctionViewOperator {


    private FunctionDefinitionOperator functionDefinitionOperator;

    public FunctionViewOperator(FunctionDefinitionOperator functionDefinitionOperator) {
        this.functionDefinitionOperator = functionDefinitionOperator;
    }

    /**
     * function cache
     */
    private LoadingCache<String, List<FunctionCategory>> functionCache =
            Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.DAYS)
                    .build(new CacheLoader<String, List<FunctionCategory>>() {
                        @Nullable
                        @Override
                        public List<FunctionCategory> load(@NonNull String s) throws Exception {
                            return listFunctionCategory(s, null);
                        }
                    });


    public List<FunctionCategory> listAviatorCategory() {
        return functionCache.get("aviator");
    }


    public List<FunctionCategory> listFunctionCategory(String functionType, String cateType) {

        Map<String, FunctionCategory> categoryMap = new LinkedHashMap<>();

        Collection<FunctionDefinition> fds = functionDefinitionOperator.listFunctions(functionType, cateType);

        for (FunctionDefinition fd : fds) {
            FunctionCategory fc = categoryMap.getOrDefault(fd.getCateName(), new FunctionCategory());
            if (fc.getName() == null) {
                fc.setName(fd.getCateName());
                fc.setType(fd.getCateType());
                categoryMap.put(fc.getName(), fc);
            }
            fc.addFunction(fd);
        }//end for

        return new ArrayList<>(categoryMap.values());
    }


}
