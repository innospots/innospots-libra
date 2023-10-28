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

package io.innospots.base.data.body;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Raydian
 * @date 2021/1/11
 */
@Getter
@Setter
@Schema(title = "data body")
public class DataBody<T> {

    @Schema(title = "body")
    protected T body;

    @Schema(title = "start time")
    protected long startTime;

    @Schema(title = "consume")
    protected int consume;

    public DataBody() {
        startTime = System.currentTimeMillis();
    }

    public DataBody(T body) {
        this.body = body;
    }

    public void end() {
        consume = (int) (System.currentTimeMillis() - startTime);
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("body=").append(body);
        sb.append(", startTime=").append(startTime);
        sb.append(", consume=").append(consume);
        sb.append('}');
        return sb.toString();
    }
}
