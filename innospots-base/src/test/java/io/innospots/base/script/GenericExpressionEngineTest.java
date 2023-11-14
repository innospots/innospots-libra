package io.innospots.base.script;

import com.google.common.collect.ImmutableList;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/6/27
 */
public class GenericExpressionEngineTest {

    @Test
    public void test() {
//        GenericScriptExecutorManager engine = GenericScriptExecutorManager.build("ClassName123456");
//        engine.register(ScriptType.JAVA, Boolean.class, "methodCall", "return true;");
//        engine.compile();
//        IScriptExecutor expression = engine.getExecutor("methodCall");
        Map<String, Object> data = new HashMap<>();
//        Object value = expression.execute(data);
//        System.out.println(value);
    }

    @Test
    public void preScriptTest() {

        String preScript = "Map<String, Object> rep = (Map<String, Object>) httpGet(\n" +
                "        \"https://qyapi.weixin.qq.com/cgi-bin/gettoken?corpid=wwb8d813c40829fd8a&corpsecret=YOJ_LCS7qFcSFBiB9N218AQsCnsFilSvRfWq7g5U3UY\", new HashMap<>(), new HashMap<>());\n" +
                "String accessToken = \"\";\n" +
                "Map<String, Object> preResult = new HashMap<>();\n" +
                "if (rep.get(\"errmsg\") != null && rep.get(\"access_token\") != null) {\n" +
                "    if (\"ok\".equals(rep.get(\"errmsg\"))) {\n" +
                "        accessToken = rep.get(\"access_token\").toString();\n" +
                "        preResult.put(\"access_token\", accessToken);\n" +
                "    }\n" +
                "}\n" +
                "return preResult;";

//        GenericScriptExecutorManager engine = GenericScriptExecutorManager.build("ClassName123456");
//        engine.register(ScriptType.JAVA, Map.class, "methodCall", preScript);
//        engine.compile();
//        IScriptExecutor expression = engine.getExecutor("methodCall");
//        Map<String, Object> data = new HashMap<>();
//        Object value = expression.execute(data);
//        System.out.println(value);
    }

    @Test
    public void postScriptTest() {

        String body = "{\n" +
                "\"errcode\": 0,\n" +
                "\"errmsg\": \"ok\",\n" +
                "\"msgid\": \"Dv0oBVNA9p2BIWPODPqgkjshR8pxWNLPAmwSL7AldKjkTlWK9GJnBrUrr_Fz-_jfoZ8juiMxLDiDUuokJbYH8g\"\n" +
                "}";

        String postScript = "Map<String, Object> map = JSONUtils.toMap(body.toString(), String.class, Object.class);\n" +
                "map.put(\"executeTime\",System.currentTimeMillis());" +
                "        return map;";

        List<ParamField> paramFields = ImmutableList.of(
                new ParamField("body", "body", FieldValueType.OBJECT)
        );

//        GenericScriptExecutorManager engine = GenericScriptExecutorManager.build("ClassName123456");
//        engine.register(ScriptType.JAVA, Map.class, "methodCall", postScript, paramFields);
//        engine.compile();
//        IScriptExecutor expression = engine.getExecutor("methodCall");
//        Map<String, Object> data = new HashMap<>();
//        data.put("body", body);
//        Object value = expression.execute(data);
//        System.out.println(value);
    }

}