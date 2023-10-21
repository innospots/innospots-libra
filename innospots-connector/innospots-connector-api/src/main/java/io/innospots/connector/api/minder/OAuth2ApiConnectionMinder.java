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

import cn.hutool.core.net.url.UrlBuilder;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.store.CacheStoreManager;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.builder.BuilderException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static io.innospots.connector.api.minder.TokenHolder.*;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/14
 */
public class OAuth2ApiConnectionMinder extends OAuth2ClientConnectionMinder {

    private static final Logger logger = LoggerFactory.getLogger(OAuth2ApiConnectionMinder.class);

    protected final String CODE = "code";
    protected final String AUTHORITY_URL = "authority_url";
    protected final String REDIRECT_ADDRESS = "redirect_address";
    protected final String REDIRECT_URI = "redirect_uri";
    protected final String SCOPE = "scope";
    protected final String STATE = "state";
    protected final String GRANT_TYPE = "grant_type";

//    private final String OAUTH_CALLBACK = PathConstant.ROOT_PATH + "oauth2/callback?appCode=";
    private final String OAUTH_CALLBACK = PathConstant.ROOT_PATH + "oauth2/callback/";


    @Override
    public Object test(ConnectionCredential connectionCredential) {

        String clientId = connectionCredential.v(CLIENT_ID);
        String clientSecret = connectionCredential.v(CLIENT_SECRET);
        String authorityUrl = connectionCredential.v(AUTHORITY_URL);
        String refreshToken = connectionCredential.v(REFRESH_TOKEN);
        String expiresIn = connectionCredential.v(EXPIRES_IN);
        int expireSecond = expiresIn != null ? Integer.parseInt(expiresIn) : 0;
        String accessToken = connectionCredential.v(ACCESS_TOKEN);
        String ts = connectionCredential.v(TOKEN_TS);
        long tokenTs = ts != null ? Long.parseLong(ts) : 0;
        String accessTokenUrl = connectionCredential.v(TOKEN_ADDRESS);
        String redirectAddress = connectionCredential.v(REDIRECT_ADDRESS);
        String scopes = connectionCredential.v(SCOPE);
        String state = connectionCredential.v(STATE);
        String akRequestMethod = connectionCredential.v(ACCESS_TOKEN_URL_REQUEST_METHOD);

        String code = connectionCredential.v(CODE);

        String redirectUrl = connectionCredential.v(REDIRECT_URI);
        if (StringUtils.isBlank(redirectUrl)) {
            redirectUrl = redirectAddress != null ? redirectAddress + OAUTH_CALLBACK + connectionCredential.getAppNodeCode() : null;
        }

        Map<String, Object> resp = null;

        String tokenJson = CacheStoreManager.get(clientId + "_" + connectionCredential.getAppNodeCode());
        if (tokenJson != null) {
            Map<String, Object> tokenBody = JSONUtils.toMap(tokenJson);
            return true;
        }

        if (code != null && redirectUrl != null) {
            resp = fetchAccessToken(accessTokenUrl, clientId, clientSecret, code, redirectUrl, connectionCredential.getAppNodeCode(), akRequestMethod);
            if (ObjectUtils.allNotNull(resp.get(TOKEN_TS), resp.get(EXPIRES_IN))) {
                return resp;
            }
            return false;
        }

        //1. 判断refreshToken是否为空，不为空 ,如果过期或者expiresIn，则调用刷新TOKEN获取
        if (refreshToken != null && ts != null &&
                Instant.ofEpochMilli(tokenTs).plusSeconds(expireSecond).isBefore(Instant.now())) {

            refreshToken(connectionCredential.getAppNodeCode(), accessTokenUrl, refreshToken, clientId, clientSecret, akRequestMethod);
            //TODO 判断
            return true;
        }


        //2. 如果accessToken为空，refreshToken为空，则返回重定向URL地址
        if (refreshToken == null || accessToken == null) {
            return openAuthorize(accessTokenUrl, authorityUrl, clientId, clientSecret, redirectUrl, scopes, akRequestMethod);
        }

        //3. 如果accessToken不为空，也没有过期，则返回true
        if (accessToken != null) {
            return true;
        }

        return resp;
    }

    private Map<String, Object> fetchAccessToken(String accessTokenUrl,
                                                 String clientId,
                                                 String clientSecret,
                                                 String code,
                                                 String redirectUrl,
                                                 String appCode,
                                                 String akRequestMethod) {
        UrlBuilder rb = UrlBuilder.of().addQuery(CLIENT_ID, clientId)
                .addQuery(CLIENT_SECRET, clientSecret)
                .addQuery(GRANT_TYPE, "authorization_code")
                .addQuery(CODE, code)
                .addQuery(REDIRECT_URI, redirectUrl);

        TokenHolder holder = buildTokenHolder(accessTokenUrl, rb.getQueryStr(), akRequestMethod);
        return cacheToken(holder, clientId, appCode);
    }

    private void refreshToken(String appCode, String accessTokenUrl, String refreshToken, String clientId, String clientSecret, String akRequestMethod) {

        UrlBuilder rb = UrlBuilder.of().addQuery(CLIENT_ID, clientId)
                .addQuery(CLIENT_SECRET, clientSecret)
                .addQuery(GRANT_TYPE, "refresh_token")
                .addQuery("refresh_token", refreshToken);
        TokenHolder holder = buildTokenHolder(accessTokenUrl, rb.getQueryStr(), akRequestMethod);

        cacheToken(holder, clientId, appCode);
    }

    private Map<String, Object> openAuthorize(String accessTokenUrl,
                                              String authorityUrl,
                                              String clientId,
                                              String clientSecret,
                                              String redirectUri,
                                              String scopes,
                                              String akRequestMethod) {
        String state = RandomStringUtils.randomAlphabetic(6).toLowerCase();

        Map<String, Object> resp = new HashMap<>();
        resp.put(CLIENT_SECRET, clientSecret);
        resp.put(CLIENT_ID, clientId);
        resp.put(REDIRECT_URI, redirectUri);
        resp.put(TOKEN_ADDRESS, accessTokenUrl);
        resp.put(ACCESS_TOKEN_URL_REQUEST_METHOD, akRequestMethod);

        UrlBuilder rb = UrlBuilder.of(authorityUrl).addQuery(CLIENT_ID, clientId)
                .addQuery("response_type", "code")
                .addQuery("redirect_uri", redirectUri)
                .addQuery(STATE, state);
        if (scopes != null) {
            rb.addQuery(SCOPE, scopes);
        }
        resp.put(AUTHORITY_URL, rb.build());
        resp.put(STATE, state);
        CacheStoreManager.save(state, JSONUtils.toJsonString(resp));
        resp.remove(CLIENT_SECRET);
        return resp;
    }

    private Map<String, Object> cacheToken(TokenHolder holder, String clientId, String appCode) {
        Map<String, Object> resp = new HashMap<>();
        holder.fetchToken(false);
        resp = holder.buildAuthBody();
        if (MapUtils.isNotEmpty(resp)) {
            CacheStoreManager.save(clientId + "_" + appCode, JSONUtils.toJsonString(resp));
        }
        resp.remove(ACCESS_TOKEN);

        return resp;
    }


    private TokenHolder buildTokenHolder(String url, String params, String akRequestMethod) {
        return TokenHolder.build(url, params, ApiMethod.valueOf(akRequestMethod));
    }
}
