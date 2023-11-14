package io.innospots.base.script.java;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import javassist.*;
import javassist.bytecode.CodeAttribute;
import javassist.bytecode.LocalVariableAttribute;
import javassist.bytecode.MethodInfo;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import java.io.File;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author Smars
 * @date 2021/4/1
 */
public class JavaExpressionEngineTest {

    private static final Logger logger = LoggerFactory.getLogger(JavaExpressionEngineTest.class);

    @Test
    public void test2() throws Exception {
        ClassPool pool = ClassPool.getDefault();
        System.out.println(pool.appendSystemPath().toString());
        CtClass clzz = pool.get("v.Picker");

        System.out.println(clzz.getSimpleName());
    }

    @Test
    public void test() throws Exception {

        File out = new File("target/test-classes");
        System.out.println(out.getAbsolutePath());

        ClassPool pool = ClassPool.getDefault();

        pool.importPackage("io.innospots.*");
        pool.appendClassPath(new ClassClassPath(this.getClass()));

        CtClass clazz = pool.makeClass("v.Picker");
        CtField param = new CtField(pool.get("java.lang.String"), "param", clazz);
        param.setModifiers(Modifier.setPrivate(Modifier.STATIC));
        clazz.addField(param, CtField.Initializer.constant("22s"));

        // 3. 生成 getter、setter 方法
        clazz.addMethod(CtNewMethod.setter("setParm", param));
        clazz.addMethod(CtNewMethod.getter("getParm", param));

        // 4. 添加无参的构造函数
        CtConstructor cons = new CtConstructor(new CtClass[]{}, clazz);
        cons.setBody("{param = \"xiaohong\";}");
        clazz.addConstructor(cons);

        // 5. 添加有参的构造函数
        cons = new CtConstructor(new CtClass[]{pool.get("java.lang.String")}, clazz);
        // $0=this / $1,$2,$3... 代表方法参数
        cons.setBody("{$0.param = $1;}");
        clazz.addConstructor(cons);

        // 6. 创建一个名为printName方法，无参数，无返回值，输出name值
        CtMethod ctMethod = new CtMethod(CtClass.voidType, "printName", new CtClass[]{}, clazz);

        ctMethod.setModifiers(Modifier.setPublic(Modifier.STATIC));
        ctMethod.setBody("{System.out.println(param);}");
        clazz.addMethod(ctMethod);

        CtMethod stcMethod = CtNewMethod.make("public static String helloWorld(){ System.out.println(\"hello world\");return \"abc\"; }", clazz);
        clazz.addMethod(stcMethod);
        clazz.writeFile(out.getAbsolutePath());


//        clazz.writeFile();
    }


    @Test
    public void register() throws ScriptException {
//        JavaScriptExecutorManager.setPath("target/clz2", "target/clz2");
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest6");
//        ParamField p = new ParamField();
//        p.setCode("p1");
//        p.setName("p1");
//        p.setValueType(FieldValueType.STRING);
//        ParamField p2 = new ParamField();
//        p2.setCode("p2");
//        p2.setName("p2");
//        p2.setValueType(FieldValueType.STRING);
//        engine.register(String.class, "call123", "{System.out.println(p1);return \"1234985\";}", p, p2);
//        engine.compile();
//        System.out.println(engine);

    }

    public static java.lang.Void abc(String a) {
        try {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Test
    public void t2() {
//        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("abc");
//        ParamField[] fields = new ParamField[3];
//        for (int i = 0; i < fields.length; i++) {
//            fields[i] = new ParamField();
//            fields[i].setCode("v" + (i + 1));
//            fields[i].setValueType(FieldValueType.DATE_TIME);
//        }

    }

    @Test
    public void testLoad() throws ScriptException {
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest6");
//        engine.reload();
//        System.out.println(engine.isLoaded());
//        IScriptExecutor expression = engine.getExecutor("call123");
//        Object o = expression.execute("abfff112");
//        System.out.println(o);

    }

    @Test
    public void testLoadAll() throws ScriptException {
        compile2AndTest("Atest6");
    }

    private void compile2AndTest(String identifier) throws ScriptException {
//        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build(identifier);
//        long s = System.currentTimeMillis();
//        engine.compile();
//        System.out.println(engine.isLoaded());
//        long e = System.currentTimeMillis() - s;
//        System.out.println(e);
//        System.out.println(engine.dump());
//        IScriptExecutor expression = engine.getExecutor("call123");
//        if (expression != null) {
//            Object o = expression.execute("abfff112");
//            System.out.println(o);
//        }
    }

    private void build2(String path, int count) throws ScriptException {
//        JavaScriptExecutorManager.setPath(path, path);
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest" + count);
//        System.out.println(engine);
//        ParamField p = new ParamField();
//        p.setCode("param");
//        p.setName("param");
//        p.setValueType(FieldValueType.STRING);
//        String src = "System.out.println(param);\nreturn param;\n";
//        String mName = "call0";
//        engine.register(String.class, mName, src, p);
//        long s = System.currentTimeMillis();
//        for (int i = 1; i <= count; i++) {
//            src = "{int p1 = 3;param+=p1;for(int j=0;j<30;j++){param+=j;};return param;}";
////            src = "{System.out.println(param+"+i+");return param+"+i+";}";
//            mName = "call" + i;
//            engine.register(String.class, mName, src, p);
//        }
//        long e = System.currentTimeMillis() - s;
//        System.out.println(e);
//        s = System.currentTimeMillis();
//        engine.compile();
//        long ee = System.currentTimeMillis() - s;
//        System.out.println(ee);
    }

    @Test
    public void testMulti() throws ScriptException {
//        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
//        Integer count = 6;
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest" + count);
//        System.out.println(engine);
//        ParamField p = new ParamField();
//        p.setCode("param");
//        p.setName("param");
//        p.setValueType(FieldValueType.STRING);
//        String src = "System.out.println(param);\nreturn param;\n";
//        String mName = "call0";
//        engine.register(String.class, mName, src, p);
//        long s = System.currentTimeMillis();
//        for (int i = 1; i <= count; i++) {
//            src = "{System.out.println(param+" + i + ");return param+" + i + ";}";
//            mName = "call" + i;
//            engine.register(String.class, mName, src, p);
//        }
//        long e = System.currentTimeMillis() - s;
//        System.out.println(e);
//        s = System.currentTimeMillis();
//        engine.reload();
//        long ee = System.currentTimeMillis() - s;
//        System.out.println(ee);
    }

    @Test
    public void testMultiThread() throws InterruptedException, ScriptException {
//        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
//        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest20000");
//        System.out.println(engine);
//        ParamField p = new ParamField();
//        p.setCode("param");
//        p.setName("param");
//        p.setValueType(FieldValueType.STRING);
//        String src = "{System.out.println(param);return param;}";
//        String mName = "call";
//        engine.register(String.class, mName, src, p);
//        long s = System.currentTimeMillis();
//        ThreadTaskExecutor executor = ThreadPoolBuilder.build("test_engine", 30000);
//        AtomicInteger counter = new AtomicInteger();
//        CountDownLatch latch = new CountDownLatch(20000);
//        for (int i = 1; i <= 20000; i++) {
//            executor.execute(() -> {
//                int c = counter.incrementAndGet();
//                String src1 = "{System.out.println(param+" + c + ");return param+" + c + ";}";
//                String mn = "call" + c;
//                //System.out.println(mn);
//                engine.register(String.class, mn, src1, p);
//                latch.countDown();
//            });
//
//        }
//        latch.await();
//        long e = System.currentTimeMillis() - s;
//        System.out.println(e);
//        s = System.currentTimeMillis();
//        engine.reload();
//        long ee = System.currentTimeMillis() - s;
//        System.out.println(ee);
    }

    @Test
    public void buildTest() throws ScriptException {
        build2("target/classes", 3);
    }

    /*
    @Test
    public void compileTest() throws ScriptException {
        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest7");
        long s = System.currentTimeMillis();
        engine.compile();
        System.out.println(engine.isLoaded());
        long e = System.currentTimeMillis() - s;
        System.out.println(e);
        System.out.println(engine.dump());

        IScriptExecutor expression = engine.getExecutor("call6");
        if (expression != null) {
            Object o = expression.execute("abfff112");
            System.out.println(o);
        } else {
            System.out.println("expression is null.");
        }
    }

    @Test
    public void testRun() throws ScriptException {
        JavaScriptExecutorManager.setPath("target/classes", "target/classes");
        JavaScriptExecutorManager engine = JavaScriptExecutorManager.build("Atest3");
        long s = System.currentTimeMillis();
        engine.reload();
        System.out.println(engine.isLoaded());
        long e = System.currentTimeMillis() - s;
        System.out.println(e);
        System.out.println(engine.dump());
        s = System.currentTimeMillis();
        IScriptExecutor expression = engine.getExecutor("call3");
        if (expression != null) {
            Object o = expression.execute("abfff112");
            e = System.currentTimeMillis() - s;
            System.out.println(o);
            System.out.println(e);
        }

        int size = 50000;
        Object o = null;
        s = System.currentTimeMillis();
        for (int i = 0; i < size; i++) {
            o = expression.execute("ade_" + i);
        }
        e = System.currentTimeMillis() - s;
        System.out.println(e);
        System.out.println(o);
        System.out.println(e * 1f / size);


    }


    @Test
    public void params() throws NotFoundException {

        try {
            ClassPool pool = ClassPool.getDefault();
            pool.importPackage("io.innospots.*");
            pool.appendClassPath(new ClassClassPath(this.getClass()));
            CtClass cc = pool.get("io.innospots.base.re.ExpressionEngineFactory");
            CtMethod cm = cc.getDeclaredMethod("build");

            // 使用javaassist的反射方法获取方法的参数名
            MethodInfo methodInfo = cm.getMethodInfo();
            CodeAttribute codeAttribute = methodInfo.getCodeAttribute();
            System.out.println(codeAttribute.getAttributes());
            System.out.println(methodInfo.getAttributes());
            LocalVariableAttribute attr = (LocalVariableAttribute) codeAttribute.getAttribute(LocalVariableAttribute.tag);
            if (attr == null) {
                // exception
            }
            String[] paramNames = new String[cm.getParameterTypes().length];
            int pos = Modifier.isStatic(cm.getModifiers()) ? 0 : 1;
            for (int i = 0; i < paramNames.length; i++)
                paramNames[i] = attr.variableName(i + pos);
            // paramNames即参数名
            for (int i = 0; i < paramNames.length; i++) {
                System.out.println(paramNames[i]);
            }

        } catch (NotFoundException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void testEqual() {
        String abc = "12345ab";
        String bc = "12345ab";
        System.out.println(abc == bc);
    }

     */
}