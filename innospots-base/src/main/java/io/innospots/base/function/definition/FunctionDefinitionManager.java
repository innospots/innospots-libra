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

package io.innospots.base.function.definition;

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 函数定义管理器
 * 分类类型管理函数定义,此处只做函数定义，函数的实现不在此处管理
 * 在markdown文件中定义函数，为了实现跨语言和环境的执行，函数可以转换为不同的表达式使用
 *
 * @author Smars
 * @date 2021/8/23
 */
public class FunctionDefinitionManager {

    private static final Logger logger = LoggerFactory.getLogger(FunctionDefinitionManager.class);

    private static LoadingCache<String, Map<String, FunctionDefinition>> functionCache =
            Caffeine.newBuilder().expireAfterAccess(1, TimeUnit.HOURS)
                    .build(new CacheLoader<String, Map<String, FunctionDefinition>>() {
                        @Override
                        public @Nullable Map<String, FunctionDefinition> load(@NonNull String s) throws Exception {
                            return loadFunctions(s);
                        }
                    });


    public static Map<String, FunctionDefinition> loadFunctions(String functionType) {
        IFunctionLoader functionLoader = BeanContextAwareUtils.getBean(IFunctionLoader.class);
        List<FunctionDefinition> functionDefinitionList = functionLoader.loadFunctions(functionType);
        return functionDefinitionList.stream().
                collect(Collectors.toMap(FunctionDefinition::getName, Function.identity()));
    }

    //public static final String FUNCTION_DEF_FILE = "/function_definition.md";

//    private static Map<String,FunctionDefinition> functions = new HashMap<>(50);

    /*
    static {
        load();
    }

    private static void load() {
        try (BufferedReader br = new BufferedReader(new InputStreamReader(
                        FunctionDefinitionManager.class
                                .getResourceAsStream(FUNCTION_DEF_FILE)))) {
            String line = br.readLine();
            line = br.readLine();
            while((line = br.readLine())!=null){
                if(StringUtils.isEmpty(line.trim())){
                    continue;
                }
                FunctionDefinition fd = new FunctionDefinition();
                String[] ss = line.split("[|]");

                fd.setCateName(ss[1].trim());
                fd.setCateType(ss[2].trim());
                fd.setName(ss[3].trim());
                fd.setReturnType(FieldValueType.getTypeByBrief(ss[4].trim()));
                fd.setDescription(ss[5].trim());
                //字段类型
                String[] vt = ss[6].trim().split(",");
                for (String s : vt) {
                    s = s.trim();
                    if(s.startsWith("*")){
                        fd.addParamType(FieldValueType.getTypeByBrief(s.substring(1)),false);
                    }else if(!s.equals("-")){
                        fd.addParamType(FieldValueType.getTypeByBrief(s),true);
                    }
                }//end for

                //fd.setAviatorExp(ss[7].trim());
                //fd.setFlinkExp(ss[8].trim());
                //fd.setSqlExp(ss[9].trim());
                //fd.setJavaExp(ss[10].trim());
                functions.put(fd.getName(),fd);
            }
            logger.info("loaded system function definition size:{}",functions.size());
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }
    }

     */

    public static Collection<FunctionDefinition> functions(String functionType) {
        Map<String, FunctionDefinition> funcMap = functionCache.get(functionType);
        if (funcMap == null) {
            return Collections.emptyList();
        }
        return funcMap.values();
    }


    /**
     * 获取函数的标准定义表达式
     *
     * @param name
     * @param functionType
     * @return
     */
    public static String getFunctionDefine(String name, String functionType) {
        Map<String, FunctionDefinition> funcMap = functionCache.get(functionType);
        if (funcMap == null) {
            return null;
        }
        FunctionDefinition fd = funcMap.get(name);

        if (fd == null) {
            return null;
        }
        String fName = fd.getExpression();
        /*
        switch (functionScript){
            case JAVA:
                fName = fd.getJavaExp();
                break;
            case SQL:
                fName = fd.getSqlExp();
                break;
            case FLINK:
                fName = fd.getFlinkExp();
                break;
            case SCRIPT:
                fName = fd.getAviatorExp();
                break;
            default:
        }

         */
        if ("-".equals(fName)) {
            return null;
        }
        return fName;
    }

}
