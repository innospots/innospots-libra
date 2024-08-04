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

import io.innospots.base.exception.AuthenticationException;
import io.innospots.libra.base.configuration.AuthProperties;
import io.innospots.libra.security.auth.model.AuthUser;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.apache.commons.lang3.RandomStringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

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

    public JwtAuthManager(AuthProperties authProperties) {
        this.authProperties = authProperties;
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


    public boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }

    public boolean isValidToken(String token) {
        Claims claims = getAllClaimsFromToken(token);
        return claims.getExpiration().before(new Date());
    }

    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }


    public JwtToken generateToken(AuthUser authUser) {
        return doGenerateToken(authUser, System.currentTimeMillis());
    }

    /*
    public JwtToken generateToken(String signKey, long ts) {
        AppAuthInfo appAuthInfo = getAuthInfoBySignKey(signKey);
        if (appAuthInfo != null) {
            return generateToken(appAuthInfo, ts);
        } else {
            return null;
        }
    }

     */

    /*
    public JwtToken generateToken(AppAuthInfo appAuthInfo, long ts) {

        JwtBuilder jbl = Jwts.builder()
                .setSubject(appAuthInfo.getName())
                .setAudience(AUDIENCE_API)
                .setId(RandomStringUtils.randomNumeric(9))
                .setIssuer(authProperties.getTokenIssuer());

        Date createdDate = new Date(ts);
        Date expirationDate = calculateExpirationDate(createdDate, appAuthInfo.getTokenExpTimeMinute());

        if (logger.isDebugEnabled()) {
            logger.debug("create token: {} , expireDate: {}", createdDate, expirationDate);
        }

        String token = jbl.setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, appAuthInfo.getSecretKey())
                .compact();

        JwtToken jwtToken = new JwtToken(token, expirationDate.getTime());
        jwtToken.setTimestamp(ts);

        return jwtToken;
    }

     */

    public JwtToken getJwtToken(String token) {
        final Claims claims = getAllClaimsFromToken(token);
        JwtToken jwtToken = new JwtToken(token, claims.getExpiration().getTime());
        jwtToken.fillClaims(claims);
        return jwtToken;
    }


    public JwtToken refreshToken(String token) {
        return refreshToken(token, null);
    }

    public JwtToken refreshToken(String token, Integer orgId) {

        final Claims claims = getAllClaimsFromToken(token);
        JwtBuilder jbl = Jwts.builder()
                .setClaims(claims);
        if (orgId != null) {
            claims.put(CLAIM_KEY_TENANT_ID, orgId);
        }
        JwtToken jwtToken = getRightToken(jbl, System.currentTimeMillis(), authProperties.getTokenSigningKey());
        jwtToken.fillClaims(claims);
        return jwtToken;
    }


    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }

    private Claims getAllClaimsFromToken(String token) {
        return Jwts.parser()
                .setSigningKey(authProperties.getTokenSigningKey())
                .parseClaimsJws(token)
                .getBody();
    }


    private JwtToken doGenerateToken(AuthUser authUser, long ts) {
        Map<String, Object> claims = new HashMap<>(3);
        claims.put(CLAIM_KEY_USER_ID, authUser.getUserId());
        claims.put(CLAIM_KEY_TENANT_ID, authUser.getLastOrgId());

        JwtBuilder jbl = Jwts.builder()
                .setClaims(claims)
                .setSubject(authUser.getUserName())
                .setAudience(AUDIENCE_WEB)
                .setId(RandomStringUtils.randomNumeric(9))
                .setIssuer(authProperties.getTokenIssuer());

        JwtToken jwtToken = getRightToken(jbl, ts, authProperties.getTokenSigningKey());
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


    private JwtToken getRightToken(JwtBuilder jbl, long ts, String secretKey) {
        Date createdDate = new Date(ts);
        Date expirationDate = calculateExpirationDate(createdDate, authProperties.getTokenExpTimeMinute());
        if (logger.isDebugEnabled()) {
            logger.debug("create token: {} , expireDate: {}", createdDate, expirationDate);
        }
        String token = jbl.setIssuedAt(createdDate)
                .setExpiration(expirationDate)
                .signWith(SignatureAlgorithm.HS512, secretKey)
                .compact();
        return new JwtToken(token, expirationDate.getTime());

    }

    public AuthProperties getAuthProperties() {
        return authProperties;
    }

    /*
    public String getPublicKey() {
        return authProperties.getPublicKey();
    }

    public String getPrivateKey() {
        return authProperties.getPrivateKey();
    }

     */

}
