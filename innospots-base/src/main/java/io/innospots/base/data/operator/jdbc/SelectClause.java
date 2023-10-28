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

import io.innospots.base.condition.BaseCondition;
import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Opt;
import io.innospots.base.condition.Relation;
import io.innospots.base.model.field.FieldValueType;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.ibatis.jdbc.SQL;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Smars
 * @date 2022/2/24
 */
@Getter
@Setter
public class SelectClause {

    protected List<String> columns = new ArrayList<>();

    protected String tableName;

    protected BaseCondition condition;

    protected List<String> orderColumns = new ArrayList<>();

    /**
     * start from zero
     */
    protected Integer page;

    protected Integer size;

    protected List<String> groupColumns = new ArrayList<>();

    public String buildSql() {
        SQL sql = new SQL();

        //StringBuilder sql = new StringBuilder();
        //sql.append("select ");
        if (CollectionUtils.isEmpty(columns)) {
            sql.SELECT("*");
        } else {
            sql.SELECT(columns.toArray(new String[]{}));
        }
        sql.FROM(tableName);

        if (condition != null) {
            String whereStmt = null;
            condition.initialize();
            whereStmt = condition.getStatement();
            if (whereStmt != null) {
                sql.WHERE(whereStmt);
            }
        }

        if (CollectionUtils.isNotEmpty(groupColumns)) {
            sql.GROUP_BY(groupColumns.toArray(new String[]{}));
        }

        if (CollectionUtils.isNotEmpty(orderColumns)) {
            sql.ORDER_BY(orderColumns.toArray(new String[]{}));
        }

        if (page != null && size != null) {
            sql.OFFSET(page * size);
        }

        if (size != null && size > 0) {
            sql.LIMIT(size);
        }
        return sql.toString();
    }

    public void addWhere(String field, Object value, Opt opt) {
        if (this.condition == null) {
            this.condition = new BaseCondition();
            this.condition.setRelation(Relation.AND);
        }
        Factor factor = new Factor();
        factor.setOpt(opt);
        factor.setCode(field);
        factor.setValue(value);
        factor.setName(field);
        factor.setValueType(FieldValueType.convertTypeByValue(value));
        this.condition.addFactor(factor);
    }

    public void addWhereInclude(String field, List<? extends Object> value, FieldValueType fieldValueType) {
        if (this.condition == null) {
            this.condition = new BaseCondition();
            this.condition.setRelation(Relation.AND);
        }
        Factor factor = new Factor();
        factor.setOpt(Opt.IN);
        factor.setCode(field);
        factor.setValue(value);
        factor.setName(field);
        factor.setValueType(fieldValueType);
        this.condition.addFactor(factor);
    }

    public void addWhereRange(String field, Object start, Object end) {
        if (this.condition == null) {
            this.condition = new BaseCondition();
            this.condition.setRelation(Relation.AND);
        }
        Factor factor = new Factor();
        factor.setOpt(Opt.BETWEEN);
        factor.setCode(field);
        List<Object> values = new ArrayList<>();
        values.add(start);
        values.add(end);
        factor.setValue(values);
        factor.setName(field);
        factor.setValueType(FieldValueType.convertTypeByValue(start));
        this.condition.addFactor(factor);
    }

    public void addOrderBy(String column) {
        if (this.orderColumns == null) {
            this.orderColumns = new ArrayList<>();
        }
        this.orderColumns.add(column);
    }

}
