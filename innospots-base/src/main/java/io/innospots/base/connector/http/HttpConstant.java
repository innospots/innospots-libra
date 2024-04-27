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

package io.innospots.base.connector.http;

/**
 * @author Smars
 * @date 2023/6/10
 */
public interface HttpConstant {

    String HTTP_API_URL = "url_address";

    String WORKFLOW_API = "workflow_api";

    String HTTP_BODY_TEMPLATE = "body_template";

    String HTTP_PREV_SCRIPT = "prev_script";

    String HTTP_POST_SCRIPT = "post_script";

    String HTTP_METHOD = "api_method";

    String KEY_USERNAME = "user_name";

    String KEY_PASSWORD = "password";

    String KEY_TOKEN = "bearer_token";

    String KEY_DIGEST_REALM = "digest_realm";

    String KEY_NONCE = "digest_nonce";

    String KEY_AUTHORITY_NAME = "authority_name";

    String KEY_AUTHORITY_VALUE = "authority_value";

    String HEADER_CONTENT_TYPE = "Content-Type";
}
