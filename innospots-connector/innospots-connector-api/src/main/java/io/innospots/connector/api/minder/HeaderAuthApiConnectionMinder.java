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

package io.innospots.connector.api.minder;

import io.innospots.base.connector.http.HttpDataConnectionMinder;
import io.innospots.base.connector.http.HttpDataExecutor;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/14
 */
public class HeaderAuthApiConnectionMinder extends HttpDataConnectionMinder {

    @Override
    protected Supplier<Map<String, String>> headers() {
        return ()->{
            HashMap<String, String> headers = new HashMap<>();
            String name = this.connectionCredential.v(HttpDataExecutor.KEY_AUTHORITY_NAME);
            String value = this.connectionCredential.v(HttpDataExecutor.KEY_AUTHORITY_VALUE);
            headers.put(name, value);
            return headers;
        };
    }


}
