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
import io.innospots.base.data.operator.IExecutionOperator;
import org.apache.commons.lang3.RandomUtils;
import org.apache.http.HttpHost;
import org.apache.http.auth.AuthScope;
import org.apache.http.auth.UsernamePasswordCredentials;
import org.apache.http.client.AuthCache;
import org.apache.http.client.CredentialsProvider;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.auth.DigestScheme;
import org.apache.http.impl.client.BasicAuthCache;
import org.apache.http.impl.client.BasicCredentialsProvider;
import org.apache.http.protocol.HttpContext;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/14
 */
public class DigestAuthApiConnectionMinder extends HttpDataConnectionMinder {


    protected HttpContext httpContext() {
        HttpHost target = new HttpHost(connectionCredential.v(HttpDataExecutor.HTTP_API_URL));
        CredentialsProvider credentialsProvider = new BasicCredentialsProvider();
        credentialsProvider.setCredentials(new AuthScope(target.getHostName(), target.getPort()),
                new UsernamePasswordCredentials(connectionCredential.v(HttpDataExecutor.KEY_USERNAME),
                        connectionCredential.v(HttpDataExecutor.KEY_PASSWORD)));
        // Create AuthCache instance
        AuthCache authCache = new BasicAuthCache();
        // Generate DIGEST scheme object, initialize it and add it to the local
        // auth cache
        DigestScheme digestAuth = new DigestScheme();
        // Suppose we already know the realm name
        digestAuth.overrideParamter("realm", connectionCredential.v(HttpDataExecutor.KEY_DIGEST_REALM, connectionCredential.getConfigCode()));
        // Suppose we already know the expected nonce value
        digestAuth.overrideParamter("nonce", Long.toString(RandomUtils.nextLong(), 36));
        authCache.put(target, digestAuth);
        // Add AuthCache to the execution context
        HttpClientContext clientContext = HttpClientContext.create();
        clientContext.setAuthCache(authCache);
        clientContext.setCredentialsProvider(credentialsProvider);

        return clientContext;
    }

    @Override
    public IExecutionOperator buildExecutionOperator() {
        if(httpDataExecutor == null){
            httpDataExecutor = new HttpDataExecutor(httpConnection,httpContext());
        }
        return httpDataExecutor;
    }
}
