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

package io.innospots.workflow.console.controller.node;

import io.innospots.base.connector.meta.CredentialAuthOption;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.base.menu.ModuleMenu;
import io.innospots.workflow.console.operator.node.FlowNodeDefinitionOperator;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2023/3/17
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "flow-node/connector/form")
@ModuleMenu(menuKey = "node-management")
@Tag(name = "Node Connector")
public class FlowNodeConnectorFormController {

    private final FlowNodeDefinitionOperator flowNodeDefinitionOperator;

    public FlowNodeConnectorFormController(
            FlowNodeDefinitionOperator flowNodeDefinitionOperator
    ) {
        this.flowNodeDefinitionOperator = flowNodeDefinitionOperator;
    }

    @GetMapping("{appNodeId}")
    @Operation(summary = "node connector form config")
    public InnospotsResponse<List<CredentialAuthOption>> selectAppNodeConnectorFormConfig(
            @Parameter(name = "appNodeId") @PathVariable Integer appNodeId
    ) {
        /*
        NodeDefinition definition = flowNodeDefinitionOperator.getNodeDefinition(appNodeId);
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

         */

        return success(null);
    }
}
