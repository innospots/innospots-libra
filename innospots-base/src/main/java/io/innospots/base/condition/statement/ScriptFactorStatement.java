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
import io.innospots.base.condition.Mode;
import io.innospots.base.condition.Opt;
import io.innospots.base.condition.Relation;
import io.innospots.base.model.field.FieldValueType;
import org.apache.commons.lang3.StringUtils;

import java.util.Collection;

/**
 * aviator 脚本表达式声明转化
 *
 * @author Smars
 * @date 2021/8/10
 */
public class ScriptFactorStatement implements IFactorStatement {

    private Mode mode = Mode.SCRIPT;

    @Override
    public Object normalizeValue(Object value, FieldValueType valueType) {
        if (valueType == null) {
            if (value instanceof Number) {
                return value;
            } else if (value != null && value.toString().endsWith("M")) {
                return value;
            } else {
                return "'" + value + "'";
            }
        } else if (valueType.isNumber()) {
            return value;
        } else {
            return "'" + value + "'";
        }
    }

    @Override
    public Object normalizeValue(Factor factor) {
        return normalizeValue(factor.getValue(), factor.getValueType());
    }

    @Override
    public String statement(Factor factor) {
        StringBuilder stmt = new StringBuilder();
        if(factor.getOpt() == null){
            return stmt.toString();
        }
        switch (factor.getOpt()) {
            case EQUAL:
            case LESS:
            case UNEQUAL:
            case GREATER:
            case LESS_EQUAL:
            case NULL:
            case NOTNULL:
            case HASVAL:
            case GREATER_EQUAL:
                stmt.append(factor.getCode()).append(factor.getOpt().symbol(mode));
                stmt.append(value(factor));
                break;
            case IN:
                stmt.append("include(seq.set(")
                        .append(factor.getValue())
                        .append("),").append(factor.getCode())
                        .append(")");
                break;
            case NOT_IN:
                stmt.append("! include(seq.set(")
                        .append(factor.getValue())
                        .append("),").append(factor.getCode())
                        .append(")");
                break;
            case BETWEEN:
                Object v1 = null;
                Object v2 = null;
                int pos = 0;
                if (factor.getValue() instanceof Collection) {
                    for (Object vs : (Collection) factor.getValue()) {
                        if (pos == 0) {
                            v1 = vs;
                        } else if (pos == 1) {
                            v2 = vs;
                        }
                        pos++;
                    }//end for
                    stmt.append(factor.getCode()).append(Opt.GREATER_EQUAL.symbol(mode)).append(normalizeValue(v1, null));
                    stmt.append(Relation.AND.symbol(mode))
                            .append(factor.getCode()).append(Opt.LESS.symbol(mode)).append(normalizeValue(v2, null));
                }
                //TODO 后续完善逻辑，判断值类型
                break;
            case LIKE:
                stmt.append(factor.getOpt())
                        .append("(").append(factor.getCode())
                        .append(",").append(value(factor))
                        .append(")");
                break;
            default:
                break;
        }
        return stmt.toString();
    }

    private Object value(Factor factor) {
        if (factor.getValueType()!=null && factor.getValueType().isNumber() && factor.getValue() == null) {
            return 0;
        } else if (factor.getValue() == null || "null".equalsIgnoreCase(String.valueOf(factor.getValue()))) {
            return "nil";
        } else if (StringUtils.isEmpty(String.valueOf(factor.getValue()))) {
            return "''";
        }

        return normalizeValue(factor.getValue(), factor.getValueType());
    }
}
