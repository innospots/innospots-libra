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


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import io.innospots.base.condition.statement.FactorStatementBuilder;
import io.innospots.base.condition.statement.IFactorStatement;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.utils.Initializer;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Getter
@Setter
@Slf4j
public class BaseCondition implements Initializer {

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    protected List<Factor> factors;

    protected Relation relation;

    @JsonIgnore
    protected String statement;

    protected Mode mode = Mode.DB;

    @JsonIgnore
    private boolean initialized;

    @JsonIgnore
    private IFactorStatement factorStatement;


    @Override
    public void initialize() {
        try {
            if (!initialized) {
                factorStatement = FactorStatementBuilder.build(mode);
                statement = rebuild().toString();
                initialized = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(),e);
            throw InnospotException.buildException(this.getClass(), ResponseCode.INITIALIZING, e);
        }
    }

    /**
     * check must
     */
    private void check() {
        if (CollectionUtils.isEmpty(factors)) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.INITIALIZING, "condition factors is empty");
        }
        if (relation == null) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.INITIALIZING, "condition relation is null");
        }
    }

    public void addFactor(Factor factor) {
        if (factors == null) {
            factors = new ArrayList<>();
        }
        factors.add(factor);
    }


    public void merge(BaseCondition condition) {
        if (factors == null) {
            factors = condition.factors;
        } else if (condition.factors != null) {
            factors.addAll(condition.factors);
        }
    }


    protected StringBuilder rebuild() {

        StringBuilder buf = new StringBuilder();
        if (CollectionUtils.isNotEmpty(factors)) {
            if (factors.size() == 1) {
                buf.append(factorStatement.statement(factors.get(0)));
            } else {
                for (int i = 0; i < factors.size(); i++) {
                    Factor factor = factors.get(i);
                    buf.append(factorStatement.statement(factor));
                    if (i < factors.size() - 1) {
                        buf.append(relation.symbol(mode));
                    }
                }
            }

        }//end factors if
        return buf;
    }

    public String statement() {
        return this.statement;
    }

}
