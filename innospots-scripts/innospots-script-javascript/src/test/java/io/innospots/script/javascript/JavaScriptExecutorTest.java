package io.innospots.script.javascript;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.java.ScriptMeta;
import org.junit.jupiter.api.Test;

import javax.script.*;
import java.lang.reflect.Method;
import java.util.*;

/**
 * @author Smars
 * @date 2021/5/6
 */
public class JavaScriptExecutorTest {

    @Test
    void execute() throws NoSuchMethodException {
        JavaScriptExecutor executor = new JavaScriptExecutor();
        Method method2 = JavaScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(method2);
        Map<String,Object> input = new HashMap<>();
//        Map<String,String> i = new HashMap<>();
        input.put("k2","12345");
        input.put("k3","abc12");
        Object obj = executor.execute(input);
        System.out.println(obj);
    }

    @ScriptMeta(suffix = "js",returnType = Map.class)
    public static String scriptMethod() {
        String src = "print(item.k2); " +
                "var itm = new Object(); " +
                "itm.a=12;" +
                "itm.b='bs';" +
                "var p={};" +
                "p.k=11;p.l='9s';" +
                "print(item);" +
                " return itm;";
        src = JavaScriptExecutor.wrapSource("func","item",src);
        System.out.println(src);
        return src;
    }

    @ScriptMeta(suffix = "js",returnType = Map.class, args = {"java.util.Map items"})
    public static String scriptMethod2() {
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
        return script;
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


    @Test
    public void build22() {
//        JavaScriptScriptExecutorManager engine = JavaScriptScriptExecutorManager.build("abc");

        String src = "print(payload.k2); " +
                "var item = new Object(); " +
                "item.a=12;" +
                "item.b='bs';" +
                "var p={};" +
                "p.k=11;p.l='9s';" +
                "print(p);" +
                " return item;";
//        String src = "var p1=1; param= param+p1; print('param'); return p1;";
//        engine.register(Object.class, "test1", src);

//        IScriptExecutor expression = engine.getExecutor("test1");
        Map<String, Object> inputs = new HashMap<>();
        Map<String, Object> ov = new HashMap<>();
        ov.put("k", "k1k1");
        ov.put("k2", "k22");
        inputs.put("payload", ov);
//        Object v = expression.execute(inputs);
//        System.out.println(v.getClass());
//        System.out.println("pl:" + v);
        //ScriptObjectMirror so = (ScriptObjectMirror) v;
//        if (v instanceof Map) {
//            Map vv = new HashMap();
//            vv.putAll((Map) v);
//            System.out.println(vv);
//        }

    }

    @Test
    public void test1() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "print(a); b";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        bindings.put("a", "osd");
        bindings.put("b", 1);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);

    }

    @Test
    public void test112() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function run2(p1,p2){print(p1.substr(1)); return p2;}  run2(a,b)";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        bindings.put("a", "osd");
        bindings.put("b", 1);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void testInt() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function $fnjEwTEnK (item){item.abc=1; item.a2=parseInt(item.age)+1; print(item.a2); return item;}\n$fnjEwTEnK(item)";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        Map<String, Object> m = new HashMap<>();
        m.put("ammd", 391);
        m.put("dd", "21");
        m.put("age", 88);
        bindings.put("item", m);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void test18() {
        double df = 333.65;
        float f2 = 333.65f;
        Double d = 333.65d;
        Double di = 12.0;
        int ii = di.intValue();

        System.out.println(ii);


        System.out.println(di.intValue() == di);

        System.out.println(f2 == df);

        System.out.println(df == d.doubleValue());
        System.out.println(d.intValue() == d.longValue());
        System.out.println(d.doubleValue() == d.floatValue());
        System.out.println(d.equals(d.floatValue()));
        System.out.println(d.doubleValue());
        System.out.println(d.floatValue());

    }

    @Test
    public void test2() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function run2(p1,p2){print(p1); return p2;}  run2(a,b)";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        bindings.put("a", "osd");
        bindings.put("b", 1);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void test22() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function test1 (p1){print(p1); return 1;}\ntest1(p1)";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);
        Bindings bindings = engine.createBindings();
        Map<String, Object> m = new HashMap<>();
        m.put("dd", "21");
        bindings.put("p1", m);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void test25() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function $fnjEwTEnK (item){item.abc=1; return item;}\n$fnjEwTEnK(item)";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        Map<String, Object> m = new HashMap<>();
//        m.put("dd","21");
        bindings.put("item", m);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void test98() throws ScriptException {
        ScriptEngine engine = new ScriptEngineManager().getEngineByName("javascript");
        String script = "function $fnjEwTEnK (item){item.gender=item.id_number.substr(0,2); return item;}\n$fnjEwTEnK(item)";
//        String script = "a > b";
        CompiledScript compiledScript = ((Compilable) engine).compile(script);

        Bindings bindings = engine.createBindings();
        Map<String, Object> m = new HashMap<>();
        m.put("id_number", "500233199603210000");
        bindings.put("item", m);

        Object result = compiledScript.eval(bindings);
        System.out.println(result);
    }

    @Test
    public void test3() throws ScriptException {
//        JavaScriptScriptExecutorManager engine = JavaScriptScriptExecutorManager.build("abc");
        ParamField p = new ParamField();
        p.setCode("p1");
        p.setName("p1");
        p.setValueType(FieldValueType.STRING);

        String src = "print(p1); return 1;";
    }

}