package io.innospots.workflow.node.app;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @date 2021/5/16
 */
@Slf4j
public class BaseNodeTest {


    @Test
    public void test() throws ScriptException {
        Path p = Paths.get("", "src");
        URL url = this.getClass().getResource("/node_test/ScriptNodeTest.json");
        System.out.println(url);
        System.out.println(p.toAbsolutePath());
    }

    public static BaseNodeExecutor baseAppNode(String fileName) throws ScriptException {
        NodeInstance instance = build(fileName + ".json");
        System.out.println(instance);

        ScriptExecutorManager.setPath("target/classes", "target/classes");


        System.out.println(ScriptExecutorManager.getClassPath());
        //BaseAppNode baseNode = instance.registerToEngine(engine,instance);

        BaseNodeExecutor appNode = null;
//        Map<String, IScriptExecutorManager> engines = new HashMap<>();
        /*
        for (MethodBody methodBody : instance.expMethods()) {
            String key = "Test_"+ fileName + "_" + methodBody.getScriptType().name();

            IExpressionEngine engine = engines.get(key);
            if(engine == null){
                engine = ExpressionEngineFactory.build("Test_" + fileName, methodBody.getScriptType());
                engines.put(key,engine);
            }
            engine.register(methodBody);

        }

         */

        try {
//            engines.values().forEach(IScriptExecutorManager::build);
        }catch (Exception e){
            System.err.println(e.getMessage());
        }
//         System.out.println(engines);
        /*
        try {
            //engine.compile();

            appNode = BaseNodeExecutor.newInstance(instance);
            appNode.build("Test" + fileName, instance);


        } catch (InstantiationException | IllegalAccessException | ClassNotFoundException | NoSuchMethodException |
                 InvocationTargetException e) {
            log.error(e.getMessage());
            throw InnospotException.buildException(BaseNodeExecutor.class, ResponseCode.INITIALIZING, e);
        }

         */

        return appNode;
    }


    public static NodeInstance build(String fileName) {
        String uri = "/node_test/" + fileName;
        NodeInstance instance = null;
        try {
            System.out.println(uri);
            URL url = BaseNodeTest.class.getResource(uri);
            if (url == null) {
                throw new RuntimeException("文件不存在：" + url);
            }
            Path p = Paths.get(url.toURI());
            System.out.println("file:" + p.toString());
            byte[] bytes = Files.readAllBytes(p);

            instance = JSONUtils.parseObject(bytes, NodeInstance.class);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return null;
        }

        return instance;
    }
}