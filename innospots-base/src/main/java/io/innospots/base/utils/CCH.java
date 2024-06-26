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

package io.innospots.base.utils;

import java.util.HashMap;
import java.util.Map;

/**
 * current context holder
 * current user info in the current thread
 *
 * @author Raydian
 * @date 2020/6/19
 */
public class CCH {

    private static ThreadLocal<Map<String, Object>> localHolder = ThreadLocal.withInitial(HashMap::new);

    public static final String COOKIE = "Cookie";

    /**
     * current user login organization
     */
    public static String ORGANIZATION_ID_KEY = "orgId";

    /**
     * default organization
     */
    private static Integer DEFAULT_ORGANIZATION_ID = 1;

    /**
     * current user login project
     */
    public static String PROJECT_ID_KEY = "projectId";

    /**
     * default project
     */
    private static Integer DEFAULT_PROJECT_ID = 1;

    /**
     * auth user name
     */
    public static String AUTH_USER_KEY = "userName";

    /**
     * auth user primary id
     */
    public static String AUTH_USER_ID = "userId";

    /**
     * default user name
     */
    private static String DEFAULT_USER = "sys_user";

    private static String SESSION_ID = "sessionId";


    /**
     * register current thread organization and user name
     *
     * @param organizationId
     * @param authUser
     */
    public static void register(Integer organizationId, Integer projectId, String authUser, Integer userId) {
        if (organizationId != null) {
            localHolder.get().put(ORGANIZATION_ID_KEY, organizationId);
        }
        if (authUser != null) {
            localHolder.get().put(AUTH_USER_KEY, authUser);
        }
        if (projectId != null) {
            localHolder.get().put(PROJECT_ID_KEY, projectId);
        }
        if (userId != null) {
            localHolder.get().put(AUTH_USER_ID, userId);
        }
    }

    public static void register(Integer projectId) {
        register(null, projectId, null, null);
    }

    /**
     * clear auth login info
     */
    public static void unregister() {
        localHolder.get().clear();
    }

    /**
     * current login user name
     *
     * @return
     */
    public static String authUser() {
        Object v = localHolder.get().get(AUTH_USER_KEY);
        if (v == null) {
            return DEFAULT_USER;
        } else {
            return String.valueOf(v);
        }
    }

    public static Integer userId() {
        Object v = localHolder.get().get(AUTH_USER_ID);
        if (v == null) {
            return 0;
        } else {
            return (Integer) v;
        }
    }

    public static Integer projectId() {
        Object v = localHolder.get().get(PROJECT_ID_KEY);
        if (v == null) {
            return DEFAULT_PROJECT_ID;
        } else {
            return (Integer) v;
        }
    }

    /**
     * current organization id
     *
     * @return
     */
    public static Integer organizationId() {
        Object v = localHolder.get().get(ORGANIZATION_ID_KEY);
        if (v == null) {
            return DEFAULT_ORGANIZATION_ID;
        } else {
            return (Integer) v;
        }
    }

    public static String contextInfo() {
        return "authUser: " + authUser() +
                ", projectId: " + projectId();
    }

    public static String sessionId() {
        Object sid = localHolder.get().get(SESSION_ID);
        if (sid == null) {
            sid = "";
        }
        return (String) sid;
    }

    public static void sessionId(String sessionId) {
        localHolder.get().put(SESSION_ID, sessionId);
    }

}
