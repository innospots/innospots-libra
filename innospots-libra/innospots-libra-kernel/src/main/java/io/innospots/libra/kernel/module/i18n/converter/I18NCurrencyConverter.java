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

package io.innospots.libra.kernel.module.i18n.converter;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.libra.kernel.module.i18n.entity.I18nCurrencyEntity;
import io.innospots.libra.kernel.module.i18n.model.I18nCurrency;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;
import org.mapstruct.factory.Mappers;

import java.util.List;

/**
 * I18nCurrencyConvertMapper
 *
 * @author Wren
 * @date 2022/1/16-22:13
 */
@Mapper
public interface I18NCurrencyConverter extends BaseBeanConverter<I18nCurrency,I18nCurrencyEntity> {
    I18NCurrencyConverter INSTANCE = Mappers.getMapper(I18NCurrencyConverter.class);


    @Mapping(target = "currencyId", ignore = true)
    void updateEntity4Model(@MappingTarget I18nCurrencyEntity entity, I18nCurrency currency);

}
