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

package io.innospots.server.base;

import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.base.utils.time.DateTimeUtils;
import io.innospots.server.base.configuration.BaseServerConfiguration;
import io.innospots.server.base.configuration.DatasourceConfiguration;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.actuate.info.Info;
import org.springframework.boot.actuate.info.InfoContributor;
import org.springframework.context.annotation.Import;
import org.springframework.stereotype.Component;

import java.lang.annotation.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/11/11
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
@Inherited
@Import({DatasourceConfiguration.class, BaseServerConfiguration.class, ServerConfigImporter.MyInfo.class})
public @interface ServerConfigImporter {

    /**
     * /actuator/info config
     */
    class MyInfo implements InfoContributor {
        @Override
        public void contribute(Info.Builder builder) {
            BeanContextAware context = BeanContextAwareUtils.beanContextAware();
            Map<String, String> runInfo = new LinkedHashMap<>();
            runInfo.put("applicationId", context.applicationId());
            runInfo.put("applicationName", context.getApplicationName());
            runInfo.put("upTime", DateTimeUtils.consume(context.getStartupDate()));
            runInfo.put("activeProFiles", StringUtils.join(context.activeProfiles(), "|"));
            builder.withDetail("info", runInfo);
        }
    }
}


