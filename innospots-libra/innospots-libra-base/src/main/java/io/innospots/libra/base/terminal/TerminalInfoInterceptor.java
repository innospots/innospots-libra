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

package io.innospots.libra.base.terminal;

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import eu.bitwalker.useragentutils.UserAgent;
import io.innospots.base.constant.RegexConstants;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.utils.http.HttpUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/1/3
 */
public class TerminalInfoInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(TerminalInfoInterceptor.class);

    public static final String USER_AGENT_HEADER = "user-agent";
    public static final String LANGUAGE_HEADER = "accept-language";

    private final Cache<String, TerminalInfo> terminalCache = Caffeine.newBuilder()
            .expireAfterAccess(Duration.ofHours(1))
            .build();

    @Override
    public boolean preHandle(javax.servlet.http.HttpServletRequest request, javax.servlet.http.HttpServletResponse response, Object handler) throws Exception {

        UserAgent userAgent = UserAgent.parseUserAgentString(request.getHeader(USER_AGENT_HEADER));
        TerminalInfo terminalInfo = new TerminalInfo();

        terminalInfo.setBrowser(userAgent.getBrowser().getName());
        terminalInfo.setSystem(userAgent.getOperatingSystem().getName());
        terminalInfo.setDevice(userAgent.getOperatingSystem().getDeviceType().name());
        terminalInfo.setManufacturer(userAgent.getOperatingSystem().getManufacturer().getName());
        terminalInfo.setLanguage(request.getHeader(LANGUAGE_HEADER));
        terminalInfo.setRequestPath(request.getServletPath());
        String ip = getIpAddress(request);
        TerminalInfo info = terminalCache.getIfPresent(ip);
        if (info == null) {
            terminalCache.put(ip,terminalInfo);
            //CompletableFuture.supplyAsync(() -> ipInfo(ip));
        }else{
            terminalInfo.setIp(info.getIp());
            terminalInfo.setProvince(info.getProvince());
            terminalInfo.setCity(info.getCity());
        }

        TerminalRequestContextHolder.setTerminal(terminalInfo);

        return true;
    }

    private TerminalInfo ipInfo(String ip) {
        TerminalInfo terminalInfo = new TerminalInfo();
        try {
            if (ip == null || ip.startsWith("127.0.0") || ip.startsWith("192.168.") || ip.startsWith("10.") || ip.startsWith("100.") || ip.matches("^172\\.(1[6-9]|2[0-9]|3[0-1])\\.[0-9]{1,3}\\.[0-9]{1,3}$")) {
            }else {
                String rspStr = HttpUtils.sendGet(RegexConstants.IP_URL, "ip=" + ip + "&json=true", "GBK");
                if (StringUtils.isNotBlank(rspStr)) {
                    Map<String, String> resultMap = JSONUtils.toMap(rspStr, String.class, String.class);
                    if (MapUtils.isNotEmpty(resultMap)) {
                        terminalInfo.setIp(resultMap.get("ip"));
                        terminalInfo.setProvince(resultMap.get("pro"));
                        terminalInfo.setCity(resultMap.get("city"));
                    }
                }
            }
        } catch (Exception e) {
            logger.error("get geo exception:{} ", e.getMessage());
        }
        terminalCache.put(ip, terminalInfo);
        return terminalInfo;
    }


    private String getIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("x-forwarded-for");
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        // 获取到多个ip时取第一个作为客户端真实ip
        if (StringUtils.isNotEmpty(ip) && ip.contains(",")) {
            String[] ipArray = ip.split(",");
            if (ArrayUtils.isNotEmpty(ipArray)) {
                ip = ipArray[0];
            }
        }
        return ip;
    }
}
