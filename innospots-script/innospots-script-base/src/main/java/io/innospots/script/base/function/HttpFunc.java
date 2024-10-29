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

package io.innospots.script.base.function;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.http.HttpClientBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/7/3
 */
@SuppressWarnings("unchecked")
public class HttpFunc {

    private static final Logger logger = LoggerFactory.getLogger(HttpFunc.class);

    public static Map<?, ?> httpPost(String url, Map<String, Object> params, String body) {
        try {
            String resp = HttpClientBuilder.post(url, params, body);
            return JSONUtils.parseObject(resp, Map.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Collections.emptyMap();
        }
    }

    public static Map<?, ?> httpGet(String url, Map<String, Object> params, Map<String, String> headers) {
        try {
            String resp = HttpClientBuilder.get(url, params, headers);
            return JSONUtils.parseObject(resp, Map.class);
        } catch (IOException e) {
            logger.error(e.getMessage());
            return Collections.emptyMap();
        }
    }

    public static Map<?, ?> jsonToMap(String body) {
        return JSONUtils.parseObject(body, Map.class);
    }


}
