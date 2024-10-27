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
import io.innospots.base.model.response.R;

/**
 * data crud operator
 *
 * @author Smars
 * @date 2021/5/3
 */
public interface IDataOperatorPoint extends IDataAccessPoint {


    R<Integer> insert(ItemRequest itemRequest);

    R<Integer> insert(BatchRequest batchRequest);

    R<Integer> upsert(ItemRequest itemRequest);

    R<Integer> upsert(BatchRequest batchRequest);

    R<Integer> update(ItemRequest itemRequest);

    R<Integer> update(BatchRequest batchRequest);

    R<DataBody<?>> execute(ItemRequest itemRequest);

    R<DataBody<?>> execute(BatchRequest itemRequest);

}
