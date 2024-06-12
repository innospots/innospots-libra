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

package io.innospots.base.condition;

/**
 * 操作符
 *
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
public enum Opt {

    /**
     *
     */
    GREATER(">", ">", "greater than","大于"),
    GREATER_EQUAL(">=", ">=", "greater than or equal to","大于等于"),
    LESS_EQUAL("<=", "<=", "less than or equal to","小于等于"),
    LESS("<", "<", "less than","小于"),
    EQUAL("=", "==", "equal","等于"),
    IN("in", "in", "match in set","包含"),
    NOT_IN("not in", "notin", "","不包含"),
    UNEQUAL("!=", "!=", "not equal","不等于"),
    LIKE("like", "like", "wildcard match","类似"),
    NULL("is null", "==", "null value","为空"),
    HASVAL("has value", "!=", "null value","有值"),
    NOTNULL("is not null", "!=", "not null value","不为空"),
    BETWEEN("between", "between", "two value range scope","范围");

    private String symbol;
    private String aSymbol;
    private String desc;
    private String descZh;

    Opt(String symbol, String aSymbol, String desc,String descZh) {
        this.symbol = symbol;
        this.aSymbol = aSymbol;
        this.desc = desc;
        this.descZh = descZh;
    }

    public String symbol(Mode mode) {
        if (mode == Mode.DB) {
            return this.symbol;
        } else {
            return this.aSymbol;
        }
    }

    public String desc() {
        return this.desc;
    }

    public String descZh(){
        return descZh;
    }
}
