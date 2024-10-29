package io.innospots.script.function.shift;

import io.innospots.base.model.field.ParamField;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Smars
 * @date 2023/9/3
 */
public class ShiftFunctionBuilder {


    public static IShiftFunction build(ShiftFunctionType functionType, ParamField field, Integer window) {
        IShiftFunction function = null;
        try {
            if(window == null){
                window = 0;
            }
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
