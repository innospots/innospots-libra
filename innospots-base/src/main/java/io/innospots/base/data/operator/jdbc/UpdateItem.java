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

package io.innospots.base.data.operator.jdbc;

import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Opt;
import io.innospots.base.condition.statement.IFactorStatement;
import io.innospots.base.model.field.FieldValueType;
import lombok.Getter;
import lombok.Setter;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/3
 */
@Getter
@Setter
public class UpdateItem {

    private Map<String, Object> data;

    private List<Factor> conditions = new ArrayList<>();

    public void addCondition(String code, Opt opt, Object value, FieldValueType valueType) {
        conditions.add(new Factor(code, opt, value, valueType));
    }

    public String buildSql(String tableName, IFactorStatement factorStatement) {
        SQL sql = new SQL().UPDATE(tableName);
        for (Map.Entry<String, Object> entry : data.entrySet()) {
            sql.SET(entry.getKey() + "=" + factorStatement.normalizeValue(entry.getValue()));
        }
        String[] stmtConditions = new String[conditions.size()];
        for (int j = 0; j < conditions.size(); j++) {
            stmtConditions[j] = factorStatement.statement(conditions.get(j));
        }
        sql.WHERE(stmtConditions);

        return sql.toString();
    }
}
