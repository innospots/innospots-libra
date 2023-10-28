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

package io.innospots.base.data.point;

import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BatchRequest;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.model.response.InnospotResponse;

/**
 * data crud operator
 *
 * @author Smars
 * @date 2021/5/3
 */
public interface IDataOperatorPoint extends IDataAccessPoint {


    InnospotResponse<Integer> insert(ItemRequest itemRequest);

    InnospotResponse<Integer> insert(BatchRequest batchRequest);

    InnospotResponse<Integer> upsert(ItemRequest itemRequest);

    InnospotResponse<Integer> upsert(BatchRequest batchRequest);

    InnospotResponse<Integer> update(ItemRequest itemRequest);

    InnospotResponse<Integer> update(BatchRequest batchRequest);

    InnospotResponse<DataBody<?>> execute(ItemRequest itemRequest);

    InnospotResponse<DataBody<?>> execute(BatchRequest itemRequest);

}
