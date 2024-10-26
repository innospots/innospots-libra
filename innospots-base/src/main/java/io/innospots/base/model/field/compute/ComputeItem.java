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

package io.innospots.base.model.field.compute;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;

/**
 * 计算表达式项
 *
 * @author Smars
 * @date 2021/8/23
 */
@Getter
@Setter
@Slf4j
@Schema(title = "the item that show in the compute input box")
public class ComputeItem {

    @Schema(title = "primary key，unique char")
    private String id;

    @Schema(title = "item type")
    private ItemElement element;

    @Schema(title = "input value, function expression, expression")
    private String value;

    @Schema(title = "the value show in the frontend")
    private String displayValue;

    private String nodeKey;

    @Schema(title = "the input value of function")
    private List<List<ComputeItem>> data = new ArrayList<>();

    public static ComputeItem build(ItemElement element) {
        ComputeItem item = new ComputeItem();
        item.element = element;
        return item;
    }

    public ComputeItem value(String displayValue, String value) {
        this.displayValue = displayValue;
        this.value = value;
        return this;
    }

    public ComputeItem id(String id) {
        this.id = id;
        return this;
    }

    public ComputeItem addItems(List<ComputeItem> items) {
        this.data.add(items);
        return this;
    }

    public ComputeItem addItem(ComputeItem item) {
        List<ComputeItem> items = new ArrayList<>();
        items.add(item);
        this.data.add(items);
        return this;
    }

    /**
     * 输出表达式的值
     *
     * @return
     */
    public String output(String scriptType) {
        StringBuilder output = new StringBuilder();
        switch (element) {
            case function:
                if (data != null) {
                    String v = this.value;
//                    String v = FunctionDefinitionManager.getFunctionDefine(this.displayValue, scriptType);
                    if (v == null) {
                        log.error("function isn't defined, {}, {}", this.displayValue, scriptType);
                    } else {
                        for (List<ComputeItem> items : data) {
                            StringBuilder outItem = new StringBuilder();
                            for (ComputeItem item : items) {
                                outItem.append(item.output(scriptType));
                            }
                            v = v.replaceFirst("\\$\\{.+?\\}", outItem.toString());
                        }
                    }
                    output.append(v);
                }//end if
                break;
            case expr:
                StringBuilder outItem = new StringBuilder();
                for (List<ComputeItem> items : data) {
                    for (ComputeItem item : items) {
                        outItem.append(item.output(scriptType));
                    }
                }
                output.append(outItem);
                break;
            case field:
            case input:
            case operator:
            default:
                output.append(value);
                break;
        }
        return output.toString();
    }

}
