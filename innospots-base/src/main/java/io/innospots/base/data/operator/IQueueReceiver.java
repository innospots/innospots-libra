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

package io.innospots.base.data.operator;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;

import java.util.Map;
import java.util.function.Consumer;

/**
 * @author Smars
 * @date 2021/5/10
 */
public interface IQueueReceiver extends IOperator {

    void openSubscribe(String group);

    void openSubscribe();

    void openSubscribe(String topic, String group, Long pollTimeOut);

    void assign(String topic, String group, Long pollTimeOut, Long seekOffset);

    DataBody<Map<String, Object>> receive();

    PageBody<Map<String, Object>> receive(int size);

    DataBody<Map<String, Object>> receiveLastData();

    void listen(Consumer<?> consumer);

    void close();

    boolean hasCache();

    String key();

}
