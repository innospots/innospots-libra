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

package io.innospots.libra.kernel.module.extension.registry;

import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.libra.base.configuration.InnospotsConfigProperties;
import io.innospots.libra.base.extension.ExtensionStatus;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity;
import io.innospots.libra.kernel.module.extension.operator.ExtInstallmentOperator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/12/6
 */
@Slf4j
@Component
public class LibraExtensionRegistryStarter implements ApplicationRunner {


    private ExtInstallmentOperator extInstallmentOperator;

    private InnospotsConfigProperties innospotsConfigProperties;

    public LibraExtensionRegistryStarter(ExtInstallmentOperator extInstallmentOperator,
                                         InnospotsConfigProperties innospotsConfigProperties) {
        this.extInstallmentOperator = extInstallmentOperator;
        this.innospotsConfigProperties = innospotsConfigProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!innospotsConfigProperties.isEnableAppLoadCheck()) {
            log.info("skip application check");
            return;
        }
        List<LibraExtensionProperties> appProperties = LibraClassPathExtPropertiesLoader.loadFromClassPath();
        Map<String, LibraExtensionProperties> libraAppPropertiesMap = Optional.ofNullable(appProperties).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(LibraExtensionInformation::getExtKey, item -> item));

        log.info("the size of libra applications has bean loaded in the system: {}, apps:[{}]", appProperties.size(), libraAppPropertiesMap.keySet());

        List<ExtInstallmentEntity> installmentEntityList = extInstallmentOperator.getBaseMapper().selectList(null);
        Map<String, ExtInstallmentEntity> appInstallmentEntityMap = Optional.ofNullable(installmentEntityList).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(ExtInstallmentEntity::getExtKey, item -> item));

        for (String appKey : libraAppPropertiesMap.keySet()) {
            if (!appInstallmentEntityMap.containsKey(appKey)) {
                log.warn("app {} has bean loaded in the system，but not installed", appKey);
            }
        }

        for (ExtInstallmentEntity app : appInstallmentEntityMap.values()) {
            if (!libraAppPropertiesMap.containsKey(app.getExtKey())) {
                log.warn("app {} has installed, but not bean loaded in the system", app.getExtKey());
            }
            if (ExtensionStatus.INSTALLED.equals(app.getExtensionStatus())) {
                UpdateWrapper<ExtInstallmentEntity> wrapper = new UpdateWrapper<>();
                wrapper.lambda().eq(ExtInstallmentEntity::getExtInstallmentId, app.getExtInstallmentId())
                        .set(ExtInstallmentEntity::getExtensionStatus, ExtensionStatus.ENABLED);
                this.extInstallmentOperator.update(null, wrapper);
            }

        }


    }
}
