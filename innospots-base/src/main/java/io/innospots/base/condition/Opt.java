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
    GREATER(">", ">", "greater than"),
    GREATER_EQUAL(">=", ">=", "greater than or equal to"),
    LESS_EQUAL("<=", "<=", "less than or equal to"),
    LESS("<", "<", "less than"),
    EQUAL("=", "==", "equal"),
    IN("in", "in", "match in set"),
    NOT_IN("not in", "notin", ""),
    UNEQUAL("!=", "!=", "not equal"),
    LIKE("like", "like", "wildcard match"),
    NULL("is null", "==", "null value"),
    HASVAL("has value", "!=", "null value"),
    NOTNULL("is not null", "!=", "not null value"),
    BETWEEN("between", "between", "two value range scope");

    private String symbol;
    private String aSymbol;
    private String desc;

    Opt(String symbol, String aSymbol, String desc) {
        this.symbol = symbol;
        this.aSymbol = aSymbol;
        this.desc = desc;
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
}
