package io.innospots.base.script.cmdline;

import cn.hutool.core.lang.Assert;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.script.IScriptExecutor;
import org.junit.jupiter.api.Test;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/11
 */
class CmdlineExpressionEngineTest {


    @Test
    void test1() {
        String cmd = "sh";
        String identifier = "Flow_190_0";
        ScriptType scriptType = ScriptType.SHELL;
        String scriptPath = "/tmp";
        String suffix = "sh";
        CmdlineScriptExecutorManager engine = CmdlineScriptExecutorManager.build(cmd, scriptPath, identifier, scriptType.name(), suffix);
//        String script = "echo 'hello world!'";
        String script = "echo $1 $2;";
        String method = "_fn56UQ5vW";
//        engine.deleteBuildFile();
//        engine.register(Void.class, method, script);
//        engine.compile();
        IScriptExecutor expression = engine.getExecutor(method);
        Assert.notNull(expression, "expression not null.");
        if (expression != null) {
            Object obj = expression.execute("11", 22, "33");
            System.out.println("----------");
            System.out.println(obj);
        }
    }

    @Test
    void testPy() {
        String cmd = "python";
        String identifier = "Flow_256_0";
        ScriptType scriptType = ScriptType.PYTHON;
        String scriptPath = "/tmp";
        String suffix = "py";
        CmdlineScriptExecutorManager engine = CmdlineScriptExecutorManager.build(cmd, scriptPath, identifier, scriptType.name(), suffix);
//        String script = "echo 'hello world!'";
        String script = "print(\"Hello, World!\"); counter =100;print(counter+201);";
        String method = "_fn79es5Ar";
//        engine.deleteBuildFile();
//        engine.register(Void.class, method, script);
//        engine.compile();
        IScriptExecutor expression = engine.getExecutor(method);
        Assert.notNull(expression, "expression not null.");
        if (expression != null) {
            Object obj = expression.execute("11", 22, "33");
            System.out.println("----------");
            System.out.println(obj);
        }
    }

}