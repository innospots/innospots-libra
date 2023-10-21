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

import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.http.HttpData;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.exception.data.HttpConnectionException;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/14
 */
@Slf4j
public class OAuth2ClientConnectionMinder extends TokenAuthApiConnectionMinder {

    protected final String CLIENT_ID = "client_id";
    protected final String CLIENT_SECRET = "client_secret";

    protected final String GRANT_TYPE = "grant_type";


    @Override
    public Object test(ConnectionCredential connectionCredential) {
        return super.test(connectionCredential);
    }

    @Override
    protected TokenHolder buildTokenHolder(ConnectionCredential cc) {
        TokenHolder th = TokenHolder.build(cc);
        th.setTokenLoc(TokenHolder.TokenLocation.PARAM);
        th.setApiMethod(ApiMethod.POST);
        th.setTokenParam("access_token");
        String queryParam = GRANT_TYPE+"="+cc.v(GRANT_TYPE,"client_credentials")+"&"+
                CLIENT_ID+"="+cc.v(CLIENT_ID) +"&" +
                CLIENT_SECRET+"="+cc.v(CLIENT_SECRET);
        th.setQueryParam(queryParam);

        return th;
    }

}
