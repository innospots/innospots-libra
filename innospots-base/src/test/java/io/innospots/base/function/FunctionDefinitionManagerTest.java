package io.innospots.base.function;

import io.innospots.base.function.definition.loader.IFunctionLoader;
import io.innospots.base.function.definition.model.FunctionDefinition;
import io.innospots.base.function.definition.manager.FunctionDefinitionManager;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.ArrayList;
import java.util.List;


/**
 * @author Smars
 * @date 2021/8/23
 */
public class FunctionDefinitionManagerTest {


    private void setup() {
        BeanContextAware contextAware = Mockito.mock(BeanContextAware.class);
        Mockito.when(contextAware.getBean(IFunctionLoader.class))
                .thenReturn(functionLoader());
        BeanContextAwareUtils.setContextAware(contextAware);

    }

    private IFunctionLoader functionLoader() {
        return new IFunctionLoader() {
            @Override
            public List<FunctionDefinition> loadFunctions(String functionType) {
                List<FunctionDefinition> functionDefinitions = new ArrayList<>();
                return functionDefinitions;
            }
        };
    }

    private FunctionDefinition build(){
        FunctionDefinition fd = new FunctionDefinition();

        return fd;
    }


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