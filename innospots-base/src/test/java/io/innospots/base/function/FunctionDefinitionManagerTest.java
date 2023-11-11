package io.innospots.base.function;

import io.innospots.base.function.definition.model.FunctionDefinition;
import io.innospots.base.function.definition.manager.FunctionDefinitionManager;
import io.innospots.base.model.field.FieldValueType;
import org.junit.Test;

/**
 * @author Smars
 * @date 2021/8/23
 */
public class FunctionDefinitionManagerTest {

    @Test
    public void functions() {
        for (FunctionDefinition function : FunctionDefinitionManager.functions("aviator")) {
//            System.out.println(function);
            if (function.getReturnType() == null) {
                System.out.println("null:" + function);
            }
            for (FieldValueType fieldValueType : function.getParamFieldTypes()) {
                if (fieldValueType == null) {
                    System.out.println("null:" + function);
                }
            }
            System.out.println(function);
            System.out.println("------");
            System.out.println(function.getExpression());
        }
    }
}