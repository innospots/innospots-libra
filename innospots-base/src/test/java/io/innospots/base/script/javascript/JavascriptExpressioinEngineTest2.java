package io.innospots.base.script.javascript;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import org.junit.Test;

import javax.script.*;
import java.util.*;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/4/4
 */
public class JavascriptExpressioinEngineTest2 {

    @Test
    public void test2() {
        String path = "/Users/yxy/works/innospots-v3/_script_build_path";
        ScriptExecutorManager.setPath(path, path);
//        GenericScriptExecutorManager engine = GenericScriptExecutorManager.build("Flow_97_0");
//        engine.reload();
//        IScriptExecutor expression = engine.getExecutor("$fnMgtskBS");
        Map<String, Object> env = new HashMap<>();
        env.put("id_number", "500233199603210000");
        env.put("manager_id", "30");
//        Object o = expression.execute(env);

//        System.out.println(o);
    }

    @Test
    public void test11() throws ScriptException {

        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function $fnv1sCxRL (items){" +
                "print(items[0]);\n" +
                "print(items.length);\n" +
                "print(typeof items[0]);\n" +
                "print(items[0].abc);\n" +
                "print(typeof items);\n" +
                "print(items instanceof Array);\n" +
                "print(items instanceof String);\n" +
                "for(var i=0;i<items.length;i++){print(items[i])}" +
                "var jjs = JSON.stringify(items);\n" +
                "print(jjs);\n" +
                "var total = 0;\n" +
                "print(\"items:\"+JSON.stringify(items));" +
                "\n\n//var ages ={\"ages\":total};\n" +
                "return items;}\n" +
                "JSON.stringify($fnv1sCxRL(items))";

        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        List<Map<String, Object>> ll = new ArrayList<>();
        Map<String, Object> mm = new LinkedHashMap<>();
        mm.put("abc", 111);
        ll.add(mm);
        mm = new LinkedHashMap<>();
        mm.put("abc", 222);
        ll.add(mm);
        mm = new LinkedHashMap<>();
        mm.put("abc", 333);
        ll.add(mm);
        System.out.println(JSONUtils.toJsonString(ll));

        bindings.put("items", ll);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }
}
