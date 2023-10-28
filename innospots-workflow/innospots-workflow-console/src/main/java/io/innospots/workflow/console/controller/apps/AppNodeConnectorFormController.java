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

package io.innospots.workflow.console.controller.apps;

import io.innospots.base.data.schema.config.ConnectionMinderSchemaLoader;
import io.innospots.base.data.schema.config.CredentialAuthOption;
import io.innospots.base.data.schema.config.FormElement;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.operator.apps.AppNodeDefinitionOperator;
import io.innospots.workflow.core.node.apps.AppConnectorConfig;
import io.innospots.workflow.core.node.apps.AppNodeDefinition;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2023/3/17
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/connector/form")
@ModuleMenu(menuKey = "application")
@Tag(name = "Apps Node Connector")
public class AppNodeConnectorFormController {

    private final AppNodeDefinitionOperator appNodeDefinitionOperator;

    public AppNodeConnectorFormController(
            AppNodeDefinitionOperator appNodeDefinitionOperator
    ) {
        this.appNodeDefinitionOperator = appNodeDefinitionOperator;
    }

    @GetMapping("{appNodeId}")
    @Operation(summary = "app node connector form config")
    public InnospotResponse<List<CredentialAuthOption>> selectAppNodeConnectorFormConfig(
            @Parameter(name = "appNodeId") @PathVariable Integer appNodeId
    ) {
        AppNodeDefinition definition = appNodeDefinitionOperator.getNodeDefinition(appNodeId);
        List<CredentialAuthOption> configs = new ArrayList<>();

        CredentialAuthOption formConfig = null;
        if (CollectionUtils.isEmpty(definition.getConnectorConfigs())) {
            formConfig = ConnectionMinderSchemaLoader.getCredentialFormConfig(definition.getConnectorName());
            configs.add(formConfig);
            return success(configs);
        }
        String NoneCode = "none";
        for (AppConnectorConfig connectorConfig : definition.getConnectorConfigs()) {
            if (NoneCode.equals(connectorConfig.getConfigCode())) {
                formConfig = new CredentialAuthOption();
                formConfig.setCode(connectorConfig.getConfigCode());
                formConfig.setName(connectorConfig.getConfigName());
                formConfig.setElements(Collections.emptyList());
            } else {
                try {
                    CredentialAuthOption credentialAuthOption =
                            ConnectionMinderSchemaLoader.getCredentialFormConfig(definition.getConnectorName(), connectorConfig.getConfigCode());
                    formConfig = (CredentialAuthOption) credentialAuthOption.clone();
                } catch (Exception e) {
                    throw ConfigException.buildTypeException(this.getClass(), "credentialFormConfig load failed.");
                }
            }
            for (FormElement element : formConfig.getElements()) {
                Object defaultValue = connectorConfig.getValue(element.getName());
                if(defaultValue!=null){
                    element.setValue(String.valueOf(defaultValue));
                }
                element.setReadOnly(connectorConfig.isReadOnly(element.getName()));
                //element.setReadOnly(defaultValue != null);
            }//end for element

            configs.add(formConfig);
        }

        return success(configs);
    }
}
