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

package io.innospots.libra.base.extension;

/**
 * @author Smars
 * @date 2021/12/1
 */
public enum ExtensionStatus {


    /**
     * app jar download in the classpath, but not save to applicationDefinitionEntity
     */
    LOADED,
    /**
     * the extension registry in the system, save to applicationDefinitionEntity and move to classLoader
     */
    AVAILABLE,
    /**
     * extension is expired and the classes in the jar will remove from classLoader
     * Apps with status expired cannot be installed
     */
    EXPIRED,
    /**
     * the extension install in the system, save to applicationInstallmentEntity
     * Application status must be available to install
     */
    INSTALLED,
    /**
     * If the extension status is enabled, the menu, page and API of the application can be used.
     */
    ENABLED,
    /**
     * extension is disabled, which the menu, page, api will not be used.
     */
    DISABLED,
    ;


    public boolean canBeInstall() {
        return AVAILABLE.equals(this);
    }

    public boolean canDisabled() {
        return ENABLED.equals(this);
    }

    public boolean canAvailable() {
        if (INSTALLED.equals(this)) {
            return true;
        }
        return false;
    }
}
