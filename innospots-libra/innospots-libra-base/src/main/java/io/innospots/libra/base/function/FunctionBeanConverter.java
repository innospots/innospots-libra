package io.innospots.libra.base.function;

import io.innospots.base.converter.BaseBeanConverter;
import io.innospots.base.function.definition.FunctionDefinition;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

/**
 * @author Smars
 * @date 2023/10/24
 */
@Mapper
public interface FunctionBeanConverter extends BaseBeanConverter<FunctionDefinition,FunctionDefinitionEntity> {

    FunctionBeanConverter INSTANCE = Mappers.getMapper(FunctionBeanConverter.class);
}
