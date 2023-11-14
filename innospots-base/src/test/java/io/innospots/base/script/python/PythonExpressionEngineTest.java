package io.innospots.base.script.python;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.script.IScriptExecutor;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/24
 */
class PythonExpressionEngineTest {


    @Test
    void testPy() {
        String cmd = "python";
        String identifier = "Flow_256_0";
        ScriptType scriptType = ScriptType.PYTHON;
        String scriptPath = "/tmp";
        String suffix = "py";
        PythonScriptExecutorManager engine = PythonScriptExecutorManager.build(cmd, scriptPath, identifier);
//        String script = "echo 'hello world!'";
        String script = "td = {\"Alice\": 112, \"Beth\": \"9102\", \"Cecil\": \"3258\"}; print(td)";
        String method = "_fn79es5Ar";
//        engine.deleteBuildFile();
//        engine.register(Void.class, method, script);
//        engine.compile();
        IScriptExecutor expression = engine.getExecutor(method);
        if (expression != null) {
            Object obj = expression.execute("11", 22, "33");
            System.out.println("----------");
            System.out.println(obj);
        }
    }
}