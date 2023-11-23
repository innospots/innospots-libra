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

package io.innospots.workflow.core.instance.converter;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.instance.entity.NodeInstanceEntity;
import io.innospots.workflow.core.node.definition.entity.FlowNodeDefinitionEntity;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import io.innospots.workflow.core.instance.model.NodeInstance;
import org.apache.commons.collections4.MapUtils;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;
import java.util.Map;


/**
 * @author Wren
 * @date 2021-12-25 17:49:00
 */
@Mapper
public interface NodeInstanceConverter extends BaseBeanConverter<NodeInstance,NodeInstanceEntity> {

    NodeInstanceConverter INSTANCE = Mappers.getMapper(NodeInstanceConverter.class);

    String PROPS = "properties";
    String ELEMENT_WIDGET = "widget";
    String ELEMENT_CODE_TYPE = "codeType";
    String WIDGET_CODE_EDITOR = "CodeEditor";


    default NodeInstance entityToModel(NodeInstanceEntity entity, FlowNodeDefinitionEntity nodeDefinitionEntity){
        NodeInstance ni = entityToModel(entity);
        ni.setCode(nodeDefinitionEntity.getCode());
        ni.setPrimitive(nodeDefinitionEntity.getPrimitive());
        ni.setIcon(nodeDefinitionEntity.getIcon());
        ni.setNodeType(nodeDefinitionEntity.getNodeType());
//        ni.setColor(appNodeDefinition.getColor());
        /*
        if(MapUtils.isNotEmpty(nodeDefinitionEntity.getConfig()) && nodeDefinitionEntity.getConfig().containsKey(PROPS)){
            Map<String,Object> elements = (Map<String, Object>) nodeDefinitionEntity.getConfig().get(PROPS);
            for (Map.Entry<String, Object> entry : elements.entrySet()) {
                String elementName = entry.getKey();
                Map<String,Object> props = (Map<String, Object>) entry.getValue();
                if(props.containsKey(ELEMENT_WIDGET)){
                    String widgetName = (String) props.get(ELEMENT_WIDGET);
                    if(WIDGET_CODE_EDITOR.equals(widgetName)){
                        String codeType = (String) props.get(ELEMENT_CODE_TYPE);
//                        ni.addScriptType(elementName, ScriptType.valueOf(codeType));
                    }

                }//end widget and codeType
            }//end for
        }//end PROPS

         */
        return ni;
    }

    /**
     * json string to ParamField of list
     *
     * @param jsonStr
     * @return List<String>
     */
    default List<ParamField> jsonStrToParamFieldList(String jsonStr) {
        return JSONUtils.toList(jsonStr, ParamField.class);
    }

    /**
     * ParamField of list to json string
     *
     * @param list
     * @return String
     */
    default String paramFieldListToJsonStr(List<ParamField> list) {
        return JSONUtils.toJsonString(list);
    }


}
