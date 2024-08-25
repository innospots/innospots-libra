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

package io.innospots.base.condition.statement;

import io.innospots.base.condition.Factor;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.FieldValueTypeConverter;

/**
 * 字段因子声明表达式转换
 *
 * @author Smars
 * @date 2021/8/10
 */
public interface IFactorStatement {


    /**
     * format value
     *
     * @param value
     * @return
     */
    Object normalizeValue(Object value, FieldValueType valueType);

    default Object normalizeValue(Object value) {
        return normalizeValue(value, FieldValueTypeConverter.convertTypeByValue(value));
    }

    /**
     * 标准化输出值
     *
     * @param factor
     * @return
     */
    Object normalizeValue(Factor factor);

    /**
     * 输出表达式
     *
     * @return
     */
    String statement(Factor factor);

}
