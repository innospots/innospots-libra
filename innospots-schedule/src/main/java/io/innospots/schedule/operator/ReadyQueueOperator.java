/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.schedule.operator;

import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyQueueEntity;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/4
 */
public class ReadyQueueOperator {

    /**
     * fetch ready execute jobs , msg status is undead , order by created time asc
     * @param fetchSize fetch size
     * @return
     */
    public List<ReadyQueueEntity> poll(int fetchSize,List<String> groupKeys) {
        //update msg status to read and serverKey
        return null;
    }

    public void ackRead(String jobReadyKey){

    }

    public void push(ReadyQueueEntity readyQueueEntity){
        return;
    }
}
