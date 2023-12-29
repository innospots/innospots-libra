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

package io.innospots.schedule.utils;

import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/28
 */
public class ParamParser {

    private static ExpressionParser expressionParser = new SpelExpressionParser();


    public Map<String,Object> toValueMap(Map<String,Object> param){
        Map<String,Object> vParam = new HashMap<>();
        param.forEach((k,v)->{
            if(v instanceof String){
                String vv = (String) v;
                if(vv.startsWith("#{")){
                    vv = vv.substring(2,vv.length()-1);
                }else{
                    vParam.put(k,v);
                }
            }else{
                vParam.put(k,v);
            }
        });

        return vParam;
    }

}
