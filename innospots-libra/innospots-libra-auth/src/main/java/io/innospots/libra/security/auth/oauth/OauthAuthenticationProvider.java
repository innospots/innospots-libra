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

package io.innospots.libra.security.auth.oauth;

import cn.hutool.http.HttpRequest;
import cn.hutool.http.HttpResponse;
import io.innospots.base.config.InnospotsConfigProperties;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.user.UserInfo;
import io.innospots.libra.base.enums.LoginStatus;
import io.innospots.libra.base.events.LoginEvent;
import io.innospots.libra.security.auth.Authentication;
import io.innospots.libra.security.auth.AuthenticationProvider;
import io.innospots.libra.security.auth.entity.OauthUserEntity;
import io.innospots.libra.security.auth.model.AuthUser;
import io.innospots.libra.security.auth.model.LoginRequest;
import io.innospots.libra.security.auth.model.OauthUser;
import io.innospots.libra.security.config.OauthProvider;
import io.innospots.libra.security.config.OauthProviderProperties;
import io.innospots.libra.security.jwt.JwtAuthManager;
import io.innospots.libra.security.jwt.JwtToken;
import io.innospots.libra.security.operator.AuthUserOperator;
import io.innospots.libra.security.operator.OauthUserOperator;
import lombok.AllArgsConstructor;
import org.noear.snack.ONode;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/2/16
 */
@AllArgsConstructor
public class OauthAuthenticationProvider implements AuthenticationProvider {

    private JwtAuthManager authManager;

    private AuthUserOperator authUserOperator;

    private OauthUserOperator oauthUserOperator;

    private OauthProviderProperties oauthProviderProperties;

    @Override
    public Authentication authenticate(LoginRequest request) throws InnospotException {
        if(!oauthProviderProperties.isEnabled() ){
            throw AuthenticationException.buildException(this.getClass(), "oauth to enable are not supported, login failed");
        }
        List<OauthProvider> providers = oauthProviderProperties.getProviders();
        OauthProvider provider = null;
        for(OauthProvider oauthProvider : providers){
            if(oauthProvider.getProviderName().equals(request.getLoginType())){
                provider = oauthProvider;
            }
        }
        if(provider == null || provider.getTokenInfo() == null){
            throw AuthenticationException.buildException(this.getClass(), "oauth provider not found, login failed");
        }
        String token = requestToken(provider.getTokenInfo(),request.getSecurityCode());
        OauthUser oauthUser = requestUser(provider,token);

        OauthUserEntity oauthUserEntity = oauthUserOperator.getByProviderId(oauthUser.getProviderId(),oauthUser.getProvider());
        AuthUser authUser = null;
        if(oauthUserEntity != null){
            authUser = authUserOperator.get(oauthUserEntity.getSysUserId());
        }else {
            UserInfo userInfo = oauthUserOperator.createOauthUser(oauthUser);
            authUser = userInfo2AuthUser(userInfo);
        }

        JwtToken jwtToken = authManager.generateToken(authUser);
        Authentication authentication = new Authentication();
        authentication.setAuthenticated(true);
        authentication.setAuthUser(authUser);
        authentication.setRequest(request);
        authentication.setToken(jwtToken);
        EventBusCenter.getInstance().asyncPost(new LoginEvent(oauthUser.getProviderId(), LoginStatus.SUCCESS.name(), "${log.message.login.success}"));
        return authentication;
    }
    private String requestToken(OauthProvider.UrlInfo tokenInfo, String code) throws InnospotException {
        if(tokenInfo == null){
            throw AuthenticationException.buildException(this.getClass(), "oauth provider not found, login failed");
        }
        Map<String,Object> params = new HashMap<>(tokenInfo.getParams());
        for(String param : params.keySet()){
            if(params.get(param).equals("{{code}}")){
                params.put(param,code);
            }
        }
        ONode tokenNode = handleProviderUrl(tokenInfo,params);
        String accessToken = tokenNode.select(tokenInfo.getResponse().get("access_token").toString()).getString();
        return accessToken;

    }

    private OauthUser requestUser(OauthProvider provider,String accessToken) {
        if(provider == null|| provider.getUserInfo()==null){
            throw AuthenticationException.buildException(this.getClass(), "oauth provider not found, login failed");
        }
        OauthProvider.UrlInfo userInfo = provider.getUserInfo();
        Map<String,Object> params = new HashMap<>(userInfo.getParams());
        for(String param : params.keySet()){
            if(params.get(param).equals("{{accessToken}}")){
                params.put(param,accessToken);
            }
        }
        ONode userNode = handleProviderUrl(userInfo,params);
        Map<String,Object>  resPath = userInfo.getResponse();
        OauthUser oauthUser = new OauthUser();
        oauthUser.setProvider(provider.getProviderName());
        if(resPath.containsKey("email")){
            oauthUser.setProviderId(userNode.select(resPath.get("email").toString()).getString());
            oauthUser.setEmail(userNode.select(resPath.get("email").toString()).getString());
        }
        if(resPath.containsKey("pictureUrl")){
            oauthUser.setPictureUrl(userNode.select(resPath.get("pictureUrl").toString()).getString());
        }
        if(resPath.containsKey("name")){
            oauthUser.setName(userNode.select(resPath.get("name").toString()).getString());
        }
        if(resPath.containsKey("providerUuid")){
            oauthUser.setProviderUuid(userNode.select(resPath.get("providerUuid").toString()).getString());
        }
        return oauthUser;
    }

    private ONode handleProviderUrl(OauthProvider.UrlInfo urlInfo,Map<String,Object> params){
        String rspStr = "";

        if(urlInfo.getMethod().equals("POST")){
            HttpResponse httpResponse = HttpRequest.post(urlInfo.getUrl()).form(params).timeout(3 * 1000).execute();
            rspStr= httpResponse.body();
        }else if(urlInfo.getMethod().equals("GET")){
            rspStr = HttpRequest.get(urlInfo.getUrl()).form(params).timeout(3 * 1000).execute().body();
        }

        ONode resNode = ONode.load(rspStr);
        String errorMsg = resNode.select(urlInfo.getErrorResponse().get("error").toString()).getString();
        if(errorMsg != null){
            throw AuthenticationException.buildException(this.getClass(), "oauth request error, "+errorMsg);
        }
        return resNode;
    }

    private AuthUser userInfo2AuthUser(UserInfo userInfo){
        AuthUser authUser = AuthUser.builder()
                .userId(userInfo.getUserId())
                .userName(userInfo.getUserName())
                .build();
        return authUser;
    }
}
