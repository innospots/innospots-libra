package io.innospots.script.python;

import io.innospots.base.script.java.ScriptMeta;
import lombok.SneakyThrows;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
/**
 * 
 * @author Smars
 * @vesion 2.0
 * @date 2024/7/28
 */
public class GroovyScriptExecutorTest {

    @SneakyThrows
    @Test
    void test() {
        GroovyScriptExecutor executor = new GroovyScriptExecutor();
        Method method = GroovyScriptExecutorTest.class.getMethod("scriptMethod");
        executor.initialize(method);
        Map<String,Object> input = new HashMap<>();
//        Map<String,String> i = new HashMap<>();
        input.put("k2","12345");
        input.put("k3","abc12");
        Object obj = executor.execute(input);
        System.out.println(obj.getClass());
        System.out.println(obj);
    }


    @ScriptMeta(suffix = "groovy")
    public static String scriptMethod() {
        String src = String.join("\n",
                "import java.util.Date",
                "def now = new Date()",
                "println \"Current date and time: $now\"",
                "def m = [:]",
                "m.put(\"a\",\"ds\")",
                "m.put(\"b\",\"bbs\")",
                "m.put(\"c\",\"acc\")",
                "println \" item_input : $item \"",
                "return m");
        return src;
    }
  
}