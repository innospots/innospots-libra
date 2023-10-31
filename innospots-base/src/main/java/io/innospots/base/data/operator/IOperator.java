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
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.data.request.BatchRequest;
import io.innospots.base.data.request.ItemRequest;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/13
 */
public interface IOperator {

    default void open(){
    }

    default <Body,D> DataBody<D> execute(BaseRequest<Body> itemRequest){
        return null;
    }

    default <Body,D> PageBody<D> executePage(BaseRequest<Body> itemRequest){
        return null;
    }

    default void close(){
    }
}
