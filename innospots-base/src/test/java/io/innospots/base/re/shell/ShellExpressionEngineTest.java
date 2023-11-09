package io.innospots.base.re.shell;

import cn.hutool.core.lang.Assert;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.re.IExpression;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/25
 */
class ShellExpressionEngineTest {


    @Test
    void test1() {
        String cmd = "sh";
        String identifier = "Flow_190_0";
        ScriptType scriptType = ScriptType.SHELL;
        String scriptPath = "/tmp";
        String suffix = "sh";
        ShellExpressionEngine engine = ShellExpressionEngine.build(cmd, scriptPath, identifier);
//        String script = "echo 'hello world!'";
        String script = "echo $1 $2;";
        String method = "_fn56UQ5vW";
        engine.deleteBuildFile();
        engine.register(Void.class, method, script);
        engine.compile();
        IExpression expression = engine.getExpression(method);
        Assert.notNull(expression, "expression not null.");
        if (expression != null) {
            Object obj = expression.execute("11", 22, "33");
            System.out.println("----------");
            System.out.println(obj);
        }
    }

}