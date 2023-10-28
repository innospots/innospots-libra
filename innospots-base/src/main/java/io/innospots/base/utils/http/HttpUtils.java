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

package io.innospots.base.utils.http;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    /**
     * send get
     *
     * @param url
     */
    public static String sendGet(String url) {
        return sendGet(url, "");
    }

    /**
     * send get
     *
     * @param url
     * @param param name1=value1&name2=value2
     */
    public static String sendGet(String url, String param) {
        return sendGet(url, param, "UTF-8");
    }

    /**
     * send get
     *
     * @param url
     * @param param       name1=value1&name2=value2
     * @param contentType encode
     */
    public static String sendGet(String url, String param, String contentType) {
        StringBuilder result = new StringBuilder();
        BufferedReader in = null;
        try {
            String urlNameString = StringUtils.isNotBlank(param) ? url + "?" + param : url;
            logger.info("sendGet - {}", urlNameString);
            URL realUrl = new URL(urlNameString);
            URLConnection connection = realUrl.openConnection();
            connection.setRequestProperty("accept", "*/*");
            connection.setRequestProperty("connection", "Keep-Alive");
            connection.setRequestProperty("user-agent", "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1;SV1)");
            connection.connect();
            in = new BufferedReader(new InputStreamReader(connection.getInputStream(), contentType));
            String line;
            while ((line = in.readLine()) != null) {
                result.append(line);
            }
            logger.info("recv - {}", result);

        } catch (Exception e) {
            logger.error("call sendGet exception, {}", e.getMessage());

        } finally {
            try {
                if (in != null) {
                    in.close();
                }
            } catch (Exception ex) {
                logger.error("call in.close Exception, ", ex);
            }
        }
        return result.toString();
    }
}
