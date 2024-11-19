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
import io.innospots.base.config.InnospotsConfigProperties;
import io.innospots.libra.base.configuration.InnospotsConsoleProperties;
import io.innospots.libra.base.extension.ExtensionStatus;
import io.innospots.libra.base.extension.LibraClassPathExtPropertiesLoader;
import io.innospots.libra.base.extension.LibraExtensionInformation;
import io.innospots.libra.base.extension.LibraExtensionProperties;
import io.innospots.libra.kernel.module.extension.entity.ExtInstallmentEntity;
import io.innospots.libra.kernel.module.extension.operator.ExtDefinitionOperator;
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


    private final ExtInstallmentOperator extInstallmentOperator;

    private final InnospotsConfigProperties innospotsConfigProperties;

    private final ExtDefinitionOperator extDefinitionOperator;

    public LibraExtensionRegistryStarter(ExtInstallmentOperator extInstallmentOperator,
                                         ExtDefinitionOperator extDefinitionOperator,
                                         InnospotsConfigProperties innospotsConfigProperties) {
        this.extInstallmentOperator = extInstallmentOperator;
        this.extDefinitionOperator = extDefinitionOperator;
        this.innospotsConfigProperties = innospotsConfigProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        if (!innospotsConfigProperties.isExtensionLoad()) {
            log.info("skip extension check");
            return;
        }
        List<LibraExtensionProperties> extProperties = LibraClassPathExtPropertiesLoader.loadFromClassPath();
        Map<String, LibraExtensionProperties> libraExtensionPropertiesMap = Optional.ofNullable(extProperties).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(LibraExtensionInformation::getExtKey, item -> item));

        log.info("the size of libra extension has bean loaded in the system: {}, extensions:[{}]", extProperties.size(), libraExtensionPropertiesMap.keySet());

        List<ExtInstallmentEntity> installmentEntityList = extInstallmentOperator.getBaseMapper().selectList(null);
        Map<String, ExtInstallmentEntity> extInstallmentEntityMap = Optional.ofNullable(installmentEntityList).orElse(new ArrayList<>())
                .stream().collect(Collectors.toMap(ExtInstallmentEntity::getExtKey, item -> item));


        for (LibraExtensionProperties ext : libraExtensionPropertiesMap.values()) {
            extDefinitionOperator.registryExtensionDefinition(ext);
        }

        for (String extKey : libraExtensionPropertiesMap.keySet()) {
            if (!extInstallmentEntityMap.containsKey(extKey)) {
                log.warn("extension {} has bean loaded in the system，but not installed", extKey);
            }
        }

        for (ExtInstallmentEntity ext : extInstallmentEntityMap.values()) {
            if(ExtensionStatus.DISABLED.name().equals(ext.getExtensionStatus())){
                continue;
            }

            if (!libraExtensionPropertiesMap.containsKey(ext.getExtKey()) ) {
                log.warn("extension {} has installed, but not bean loaded in the system", ext.getExtKey());
            }

            if (ExtensionStatus.INSTALLED.name().equals(ext.getExtensionStatus())) {
                UpdateWrapper<ExtInstallmentEntity> wrapper = new UpdateWrapper<>();
                wrapper.lambda().eq(ExtInstallmentEntity::getExtInstallmentId, ext.getExtInstallmentId())
                        .set(ExtInstallmentEntity::getExtensionStatus, ExtensionStatus.ENABLED);
                this.extInstallmentOperator.update(null, wrapper);
            }

        }


    }
}
