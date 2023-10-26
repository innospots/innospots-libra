package io.innospots.base.function.definition;

import java.util.List;

/**
 * @author Smars
 * @date 2023/10/24
 */
public interface IFunctionLoader {

    List<FunctionDefinition> loadFunctions(String functionType);
}
