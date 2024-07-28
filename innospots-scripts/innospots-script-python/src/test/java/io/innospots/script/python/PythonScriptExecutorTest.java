package io.innospots.script.python;

import io.innospots.base.script.java.ScriptMeta;
import org.junit.jupiter.api.Test;

import javax.script.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * 
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/19
 */
class PythonScriptExecutorTest {



    @Test
    void execute() throws NoSuchMethodException {
        PythonScriptExecutor executor = new PythonScriptExecutor();
        Method method2 = PythonScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
        input.put("p1","param1");
        input.put("p2","param2");
        Object obj = executor.execute(input);
        System.out.println(obj.getClass());
        System.out.println("out:"+obj);
    }

    @ScriptMeta(scriptType = "jython",suffix = "py",returnType = Object.class,
            args = {"item"})
    public static String scriptMethod() {
        String ps =  "def add(a, b):\n" +
                "    return a + b\n" +
                "result = add(1, 2)\n" +
                "print(result) \n" +
                "print(item['p1'] +':'+item['p2']) \n" +
                "td={'Alice': 112, 'Beth': '9102', 'Cecil': '3258'};print(td)" +
                "\n" +
                "td" + "\n" + "item"
                ;
//        String ps = "td={'Alice': 112, 'Beth': '9102', 'Cecil': '3258'};print(td)";

        return ps;
    }

    @Test
    void execute2() throws NoSuchMethodException {
        PythonScriptExecutor executor = new PythonScriptExecutor();
        Method method2 = PythonScriptExecutorTest.class.getMethod("scriptMethod2");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
        input.put("p1","param1");
        input.put("p2","param2");
        Object obj = executor.execute(input);
        System.out.println("out:"+obj);
    }

    @ScriptMeta(scriptType = "jython",suffix = "py",returnType = Object.class,
            args = {"item"})
    public static String scriptMethod2() {
//        String ps = "def add(a, b):\n    return a + b\nresult = add(1, 2)\ntdict={'Name': 'Runoob'}\ntdict";
//        String ps = "td = {'Alice': 112, 'beth':'abc','cecil':'9988'};";
        String ps = "td={'Alice': 112, 'Beth': '9102', 'Cecil': '3258'}\ntd";
//        String ps = "td={'Alice': 112, 'Beth': '9102', 'Cecil': '3258'};print(td)";

        return ps;
    }


    @Test
    void test1(){
        ScriptEngineManager manager = new ScriptEngineManager();

        // 获取Python（Jython）脚本引擎
        ScriptEngine engine = manager.getEngineByName("jython");
        if (engine == null) {
            System.err.println("Jython engine not found.");
            return;
        }

        try {
            Bindings bindings = new SimpleBindings();
            // Python脚本内容，例如计算两个数的和
            String script = "def add(a, b):\n" +
                    "    return a + b\n" +
                    "result = add(1, 2)\n" +
                    "result";

            // 执行Python脚本
            Object result = engine.eval(script,bindings);

            if(result == null){
                result = bindings.get("result");
            }

            // 输出执行结果
            System.out.println("The result of the Python script is: " + result);
        } catch (ScriptException e) {
            e.printStackTrace();
        }
    }
}