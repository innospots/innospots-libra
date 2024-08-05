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

package io.innospots.libra.security.jwt;

import com.nimbusds.jose.*;
import com.nimbusds.jose.crypto.MACSigner;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWTClaimsSet;
import com.nimbusds.jwt.SignedJWT;
import io.innospots.base.exception.AuthenticationException;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.security.auth.model.AuthUser;
import jakarta.servlet.http.HttpServletRequest;
import lombok.SneakyThrows;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.text.ParseException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * authenticate jwt token validation，expired date, create token
 *
 * @author Smars
 * @date 2021/2/16
 */
public class JwtAuthManager {

    private static final Logger logger = LoggerFactory.getLogger(JwtAuthManager.class);


    static final String CLAIM_KEY_TENANT_ID = "tenant_id";
    static final String CLAIM_KEY_USER_ID = "user_id";

    public static final String AUDIENCE_WEB = "WEB";
    public static final String AUDIENCE_API = "SRV_API";

    public static final String HEADER = "Authorization";

    private AuthProperties authProperties;

    private JWSVerifier jwsVerifier;

    @SneakyThrows
    public JwtAuthManager(AuthProperties authProperties) {
        this.authProperties = authProperties;
        jwsVerifier = new MACVerifier(authProperties.getTokenSigningKey());

    }


    public String getToken() {
        ServletRequestAttributes requestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (requestAttributes == null) {
            return null;
        }
        return this.getToken(requestAttributes.getRequest());
    }

    public JwtToken validToken(HttpServletRequest request) {
        String token = getToken(request);
        return getJwtToken(token);
    }

    public String getToken(HttpServletRequest request) {
        String token = request.getHeader(HEADER);
        if (token != null && token.startsWith("Bearer ")) {
            token = token.substring(7);
        } else {
            throw AuthenticationException.buildTokenInvalidException(this.getClass(), "Not Invalid Authorization Header.");
        }
        return token;
    }


    public boolean isValidToken(String token) {
        try{
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean verify = signedJWT.verify(jwsVerifier);
           return verify && signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
        }catch (Exception e){
            logger.error(e.getMessage());
            return false;
        }
    }

    public JwtToken generateToken(AuthUser authUser) {
        try {
            return doGenerateToken(authUser, System.currentTimeMillis());
        } catch (JOSEException | ParseException e) {
            logger.error(e.getMessage());
            throw AuthenticationException.buildTokenInvalidException(this.getClass(),e.getMessage());
        }
    }


    public JwtToken getJwtToken(String token) {
        JwtToken jwtToken = null;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            boolean valid = signedJWT.verify(jwsVerifier) &&
                    signedJWT.getJWTClaimsSet().getExpirationTime().after(new Date());
            if(valid){
                jwtToken = JwtToken.build(signedJWT,token);
            }else{
                throw AuthenticationException.buildTokenInvalidException(this.getClass());
            }
        }catch (Exception e){
            logger.error(e.getMessage());
            throw AuthenticationException.buildTokenInvalidException(this.getClass(), "token is invalid.");
        }

        /*
        final Claims claims = getAllClaimsFromToken(token);
        JwtToken jwtToken = new JwtToken(token, claims.getExpiration().getTime());
        jwtToken.fillClaims(claims);

         */
        return jwtToken;
    }

    public JwtToken refreshToken(String token) {
        JwtToken jwtToken = null;
        try {
            SignedJWT signedJWT = SignedJWT.parse(token);
            Map<String,Object> claims = signedJWT.getJWTClaimsSet().getClaims();
            jwtToken = buildJwtToken(signedJWT.getJWTClaimsSet().getSubject(),System.currentTimeMillis(),claims);
        } catch (ParseException | JOSEException e) {
            logger.error(e.getMessage());
            throw AuthenticationException.buildTokenInvalidException(this.getClass(),e.getMessage());
        }
        return jwtToken;
    }



    private JwtToken doGenerateToken(AuthUser authUser, long ts) throws JOSEException, ParseException {

        Map<String, Object> claims = new HashMap<>(3);
        claims.put(CLAIM_KEY_USER_ID, authUser.getUserId());
        claims.put(CLAIM_KEY_TENANT_ID, authUser.getLastOrgId());

        JwtToken jwtToken = buildJwtToken(authUser.getUserName(),ts,claims);

        jwtToken.setAudience(AUDIENCE_WEB);
        jwtToken.setTimestamp(ts);
        jwtToken.setUserId(authUser.getUserId());
        jwtToken.setUserName(authUser.getUserName());
        jwtToken.setOrgId(authUser.getLastOrgId());
        jwtToken.setRelocation(authProperties.getSuccessPage());
        return jwtToken;
    }


    private Date calculateExpirationDate(Date createdDate, int expireTimeMinute) {
        return new Date(createdDate.getTime() + (long) expireTimeMinute * 1000 * 60);
    }


    private JwtToken buildJwtToken(String userName, long ts,Map<String,Object> claims) throws JOSEException, ParseException {
        JwtToken jwtToken = null;

            JWSHeader header = new JWSHeader.Builder(JWSAlgorithm.HS256)
                    .type(JOSEObjectType.JWT)
                    .build();
            Date createdDate = new Date(ts);
            Date expirationDate = calculateExpirationDate(createdDate, authProperties.getTokenExpTimeMinute());
            if (logger.isDebugEnabled()) {
                logger.debug("create token: {} , expireDate: {}", createdDate, expirationDate);
            }

            // create JWT Claims
            JWTClaimsSet claimsSet = new JWTClaimsSet.Builder()
                    .subject(userName)
                    .issuer(authProperties.getTokenIssuer())
                    .audience(AUDIENCE_WEB)
                    .jwtID(RandomStringUtils.randomAlphanumeric(9))
                    .expirationTime(expirationDate) // expire time
                    .build();
            claimsSet.getClaims().putAll(claims);

            // create singer
            JWSSigner signer = new MACSigner(authProperties.getTokenSigningKey());

            // create JWT
            SignedJWT signedJWT = new SignedJWT(header, claimsSet);
            signedJWT.sign(signer);

            // build jwt
            String jwtString = signedJWT.serialize();
            jwtToken =JwtToken.build(signedJWT,jwtString);

        return jwtToken;
    }

}
