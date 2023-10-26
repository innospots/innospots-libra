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

package io.innospots.base.constant;

/**
 * Constant
 *
 * @author Jegy
 * @date 2022/06/04
 */
public class RegexConstants {

    public static final String CN_EN_NUM = "[\u4e00-\u9fa5\\w]+";

    public static final String EN_NUM = "[\\w]*";

    public static final String REXP_EMAIL = "[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+(@)[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)+(?![-_])";

    public static final String REXP_PHONE = "^$|^1[3|456789][0-9]\\d{4,8}$";

    public static final String REXP_URL = "^(https?|http?|ftp|file)://.+";

    public static final String IP_URL = "http://whois.pconline.com.cn/ipJson.jsp";

    public static final String NEWS_URL = "https://innospots.com/wp-json/wp/v2/posts?categories=3&per_page=3&_fields=id,date,link,title,_links.wp:featuredmedia,_embeded&_embed=wp:featuredmedia";

    public static final String ACTIVITY_URL = "https://innospots.com/wp-json/wp/v2/posts?categories=4";

    /**
     * only supports: contains a maximum of 32 characters, including letters, digits, and underscores (_), and chinese.
     */
    public final static String NAME_REGEX = "^[\\u4E00-\\u9FA5A-Za-z0-9_]{1,32}$";

    /**
     * only supports: contains a maximum of 64 characters, including letters, digits, and underscores (_), and chinese. It can't all be numbers.
     */
    public final static String CODE_REGEX = "^[a-zA-Z]\\w{1,64}$";

}