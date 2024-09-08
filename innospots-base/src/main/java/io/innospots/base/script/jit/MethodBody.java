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

package io.innospots.base.script.jit;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.OutputMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * 构造方法结构体
 *
 * @author Smars
 * @date 2021/8/31
 */
@Getter
@Setter
@AllArgsConstructor
@Builder
public class MethodBody {

    /**
     * return class type
     */
    private Class<?> returnType;

    /**
     * method name
     */
    private String methodName;

    /**
     * script source
     */
    private String srcBody;

    /**
     * input params
     */
    private List<ParamField> params;

    /**
     * script type
     */
    private String scriptType;

    /**
     * file type suffix
     */
    private String suffix;

    /**
     * cmd path
     */
    private String cmdPath;

    private OutputMode outputMode;


    public MethodBody() {
    }


    public String scriptName(){
        return methodName + "." +suffix;
    }
}
