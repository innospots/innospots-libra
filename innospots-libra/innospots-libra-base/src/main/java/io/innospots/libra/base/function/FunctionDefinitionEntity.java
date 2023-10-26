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

package io.innospots.libra.base.function;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.innospots.base.function.definition.FunctionDefinition;
import io.innospots.base.model.field.FieldValueType;
import lombok.Getter;
import lombok.Setter;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * function definition
 *
 * @author Smars
 * @date 2021/8/22
 */
@Entity
@Table(name = FunctionDefinitionEntity.TABLE_NAME)
@TableName(value = FunctionDefinitionEntity.TABLE_NAME)
@Setter
@Getter
public class FunctionDefinitionEntity {

    public static final String TABLE_NAME = "sys_function";

    @Id
    @TableId(type = IdType.AUTO)
    @Column
    private Integer functionId;

    @Column(length = 16)
    private String cateType;

    @Column(length = 16)
    private String cateName;

    @Column(length = 64)
    private String name;

    @Column(length = 128)
    private String description;

    @Column(length = 16)
    private String returnType;

    /**
     * split by comma, the values sequence is FieldValueType.
     * exp: string,object,long,number,datetime,timestamp,int,double,boolean
     * if the param start with '*' , which presents is optional.
     */
    @Column(length = 128)
    private String paramTypes;

    @Column(length = 2048)
    private String expression;

    /**
     * the values: java, aviator, flink, sql, mysql etc.
     */
    @Column(length = 16)
    private String functionType;

    public FunctionDefinition toModel() {
        FunctionDefinition fd = new FunctionDefinition();
        fd.setCateName(this.cateName);
        fd.setCateType(this.cateType);
        fd.setName(this.name);
        fd.setDescription(this.description);
        fd.setReturnType(this.returnType);
        fd.setExpression(this.expression);
        fd.setFunctionType(this.functionType);

        String[] vt = paramTypes.trim().split(",");
        for (String s : vt) {
            s = s.trim();
            if (s.startsWith("*")) {
                fd.addParamType(FieldValueType.getTypeByBrief(s.substring(1)), false);
            } else if (!"-".equals(s)) {
                fd.addParamType(FieldValueType.getTypeByBrief(s), true);
            }
        }//end for

        return fd;
    }

}
