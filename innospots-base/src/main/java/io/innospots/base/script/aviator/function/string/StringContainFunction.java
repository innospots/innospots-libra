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

package io.innospots.base.script.aviator.function.string;

import com.googlecode.aviator.runtime.function.AbstractFunction;
import com.googlecode.aviator.runtime.function.FunctionUtils;
import com.googlecode.aviator.runtime.type.AviatorBoolean;
import com.googlecode.aviator.runtime.type.AviatorObject;

import java.util.Map;

/**
 * 判断字符串是否包含在另一字符串中
 * like判断
 *
 * @author Smars
 * @date 2021/9/4
 */
public class StringContainFunction extends AbstractFunction {

    @Override
    public String getName() {
        return "string.contain";
    }

    @Override
    public AviatorObject call(Map<String, Object> env, AviatorObject arg1, AviatorObject arg2) {

        String source = FunctionUtils.getStringValue(arg1, env);
        String target = FunctionUtils.getStringValue(arg2, env);

        if (source != null && target != null) {
            if (source.contains(target)) {
                return AviatorBoolean.TRUE;
            } else {
                return AviatorBoolean.FALSE;
            }
        }
        return AviatorBoolean.FALSE;
    }
}
