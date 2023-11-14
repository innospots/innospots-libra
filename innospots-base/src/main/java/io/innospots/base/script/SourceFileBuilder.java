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

import io.innospots.base.model.field.ParamField;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/5
 */
public interface SourceFileBuilder {


    void addMethod(Class<?> returnType, String body, String methodName, ParamField... params);


    default void writeToFile() throws IOException {
    }

    void deleteSourceFile();

    /**
     * build script source
     *
     * @return
     */
    String toSource();

    void clear();


    default String tab(int size) {
        return StringUtils.leftPad("", size);
    }
}
