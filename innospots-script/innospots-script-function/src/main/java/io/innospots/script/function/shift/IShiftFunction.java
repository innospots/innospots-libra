package io.innospots.script.function.shift;

import io.innospots.base.model.field.ParamField;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/9/3
 */
public interface IShiftFunction {

    Object[] compute(List<Map<String,Object>> items);

    int window();

    String fieldCode();

    void initialize(ParamField field);
}
