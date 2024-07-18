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

package io.innospots.base.i18n;

/**
 * I18nUtils
 *
 * @author Wren
 * @date 2022/2/5-21:02
 */
public class I18nUtils {
    private static final String PRE_CST = "${";
    private static final String SUB_CST = "}";

    /**
     * Whether to I18N convert field
     *
     * @param field
     * @return
     */
    public static boolean isI18nField(String field) {
        return field != null && field.startsWith(PRE_CST) && field.endsWith(SUB_CST);
    }

    /**
     * Get the field name of I18N
     *
     * @param name
     * @return
     */
    public static String getI18nName(String name) {
        return name != null && isI18nField(name) ? name.substring(PRE_CST.length(), name.length() - SUB_CST.length()) : null;
    }

    public static String wrapKey(String value){
        return PRE_CST + value + SUB_CST;
    }
}
