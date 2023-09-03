package io.innospots.base.function.shift;

import io.innospots.base.function.moving.IMovingFunction;
import io.innospots.base.model.field.ParamField;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class ShiftFunctionBuilder {


    public static IShiftFunction build(ShiftFunctionType functionType, ParamField field, int window) {
        IShiftFunction function = null;
        try {
            Constructor<? extends IShiftFunction> constructor = functionType.shiftFunctionClass().getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            function = constructor.newInstance(window);
            function.initialize(field);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException |
                 InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return function;
    }
}
