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

package io.innospots.libra.base.function;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.function.definition.FunctionDefinition;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/6/25
 */
public class FunctionDefinitionOperator extends ServiceImpl<FunctionDefinitionDao, FunctionDefinitionEntity> {


    public boolean createFunction(FunctionDefinition functionDefinition) {
        QueryWrapper<FunctionDefinitionEntity> queryWrapper = new QueryWrapper<>();
        queryWrapper.lambda().eq(FunctionDefinitionEntity::getFunctionType, functionDefinition.getFunctionType())
                .eq(FunctionDefinitionEntity::getName, functionDefinition.getName());
        long count = this.count(queryWrapper);
        if (count > 0) {
            throw ResourceException.buildExistException(this.getClass(), functionDefinition.getName(), functionDefinition.getFunctionType());
        }

        return this.save(FunctionBeanConverter.INSTANCE.modelToEntity(functionDefinition));
    }

    public boolean updateFunction(FunctionDefinition functionDefinition) {
        if (functionDefinition.getFunctionId() == null) {
            throw ResourceException.buildUpdateException(this.getClass(), "functionId is missing");
        }
        return this.updateById(FunctionBeanConverter.INSTANCE.modelToEntity(functionDefinition));
    }

    public boolean deleteFunction(Integer functionId) {
        return this.removeById(functionId);
    }


    /**
     * list functions using expressionType
     *
     * @param expressionType
     * @return
     */
    public List<FunctionDefinition> listFunctions(String expressionType, String cateType) {
        QueryWrapper<FunctionDefinitionEntity> queryWrapper = new QueryWrapper<>();
        if (expressionType != null) {
            queryWrapper.lambda().eq(FunctionDefinitionEntity::getFunctionType, expressionType);
        }
        if (cateType != null) {
            queryWrapper.lambda().eq(FunctionDefinitionEntity::getCateType, cateType);
        }
        List<FunctionDefinitionEntity> functionDefinitionEntities = this.list(queryWrapper);
        if (CollectionUtils.isEmpty(functionDefinitionEntities)) {
            return Collections.emptyList();
        }
        return functionDefinitionEntities.stream().map(FunctionDefinitionEntity::toModel).collect(Collectors.toList());
    }

}
