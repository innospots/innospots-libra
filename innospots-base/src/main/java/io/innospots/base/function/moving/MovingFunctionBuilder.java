package io.innospots.base.function.moving;

import io.innospots.base.function.StatisticFunctionType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * @author Smars
 * @date 2023/9/2
 */
public class MovingFunctionBuilder {

    public static IMovingFunction build(StatisticFunctionType functionType,
                                             int window,
                                             ParamField summaryField,
                                             ParamField weightField,
                                             IScriptExecutor condition){
        IMovingFunction function = null;
        try {
            Constructor<? extends IMovingFunction> constructor = functionType.movingFuncClass().getDeclaredConstructor(int.class);
            constructor.setAccessible(true);
            function = constructor.newInstance(window);
            function.initialize(summaryField,weightField,condition);
        } catch (InstantiationException | IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            throw new RuntimeException(e);
        }
        return function;
    }
    public static IMovingFunction build(StatisticFunctionType functionType,
                                        int window,
                                        ParamField summaryField,
                                        ParamField weightField){
        return build(functionType,window,summaryField,weightField,null);
    }
}
