package io.innospots.workflow.node.app.compute;

import io.innospots.base.condition.Factor;
import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.model.field.ParamField;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2023/9/3
 */
@Getter
@Setter
public class FunctionField {

    protected ParamField field;

    protected ParamField weight;

    protected String fieldCode;

    protected StatisticFunctionType function;

    protected Integer winLength;

}
