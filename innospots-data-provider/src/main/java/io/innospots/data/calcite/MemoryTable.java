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

package io.innospots.data.calcite;

import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.FieldValueTypeConverter;
import lombok.Getter;
import org.apache.calcite.DataContext;
import org.apache.calcite.adapter.java.JavaTypeFactory;
import org.apache.calcite.linq4j.AbstractEnumerable;
import org.apache.calcite.linq4j.Enumerable;
import org.apache.calcite.linq4j.Enumerator;
import org.apache.calcite.rel.type.RelDataType;
import org.apache.calcite.rel.type.RelDataTypeFactory;
import org.apache.calcite.schema.ScannableTable;
import org.apache.calcite.schema.impl.AbstractTable;
import org.apache.calcite.sql.type.SqlTypeUtil;
import org.apache.calcite.util.Pair;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/10/13
 */
public class MemoryTable extends AbstractTable implements ScannableTable {


    @Getter
    public final String tableName;

    @Getter
    private List<Column> columns;

    private List<Map<String, Object>> items;

    public MemoryTable(String tableName) {
        this.tableName = tableName;
    }

    public void setItems(List<Map<String, Object>> items) {
        this.items = items;
        this.columns = extractColumn(items.get(0));
    }

    public MemoryTable(String tableName,
                       List<Column> columns, List<Map<String, Object>> items) {
        this.tableName = tableName;
        this.columns = columns;
        this.items = items;
    }

    public MemoryTable(String tableName, List<Map<String, Object>> items) {
        this(tableName, extractColumn(items.get(0)), items);
    }


    public static List<Column> extractColumn(Map<String, Object> item) {
        List<Column> cols = new ArrayList<>();
        for (Map.Entry<String, Object> objectEntry : item.entrySet()) {
            Column column = new Column();
            column.setCode(objectEntry.getKey());
            column.setName(objectEntry.getKey());
            column.setValueType(FieldValueTypeConverter.convertJavaTypeByValue(objectEntry.getValue()));
            cols.add(column);
        }
        return cols;
    }

    @Override
    public RelDataType getRowType(RelDataTypeFactory relDataTypeFactory) {
        JavaTypeFactory typeFactory = (JavaTypeFactory) relDataTypeFactory;
        //column name
        List<String> names = new ArrayList<>();
        //column type
        List<RelDataType> types = new ArrayList<>();
        for (Column col : columns) {
            names.add(col.getCode());
            RelDataType relDataType = typeFactory.createJavaType(col.getValueType().getClazz());
            relDataType = SqlTypeUtil.addCharsetAndCollation(relDataType, typeFactory);
            types.add(relDataType);
        }
        return typeFactory.createStructType(Pair.zip(names, types));
    }

    @Override
    public Enumerable<Object[]> scan(DataContext dataContext) {
        return new AbstractEnumerable<Object[]>() {
            @Override
            public Enumerator<Object[]> enumerator() {
                return new MemoryEnumerator(items, columns);
            }
        };
    }
}
