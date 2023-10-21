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


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Raydian
 * @version 1.0.0
 * @date 2020/11/3
 */
@Getter
@Setter
public class EmbedCondition extends BaseCondition {

    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    protected List<EmbedCondition> embeds;

    public void addCondition(EmbedCondition condition) {
        if (embeds == null) {
            this.embeds = new ArrayList<>();
        }
        condition.setMode(this.mode);
        this.embeds.add(condition);
    }

    @Override
    public void merge(BaseCondition condition) {
        super.merge(condition);

        if (condition instanceof EmbedCondition) {
            if (embeds == null) {
                embeds = ((EmbedCondition) condition).embeds;
            } else if (((EmbedCondition) condition).embeds != null) {
                embeds.addAll(((EmbedCondition) condition).embeds);
            }
        }

    }


    @Override
    protected StringBuilder rebuild() {
        StringBuilder buf = super.rebuild();

        if (CollectionUtils.isNotEmpty(embeds)) {
            boolean brave = false;
            if (buf.length() > 0) {
                buf.append(relation.symbol(mode));
                brave = true;
            }
            if (brave) {
                buf.append("(");
            }
            for (int i = 0; i < embeds.size(); i++) {
                BaseCondition condition = embeds.get(i);
                condition.setMode(this.mode);
                condition.initialize();
                if(StringUtils.isNotEmpty(condition.statement)){
                    buf.append("(");
                    buf.append(condition.statement);
                    buf.append(")");
                }
                if (i < embeds.size() - 1) {
                    buf.append(relation.symbol(mode));
                }
            }
            if (brave) {
                buf.append(")");
            }
        }//end embeds

        return buf;
    }

}
