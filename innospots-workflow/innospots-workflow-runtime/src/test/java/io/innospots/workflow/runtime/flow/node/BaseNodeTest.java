package io.innospots.workflow.runtime.flow.node;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Smars
 * @date 2021/5/16
 */
@Slf4j
public class BaseNodeTest {


    @Test
    void test() throws ScriptException {
        Path p = Paths.get("", "src");
        URL url = this.getClass().getResource("/node_test/script/ScriptJavaNodeSample.json");
        System.out.println(url);
        System.out.println(p.toAbsolutePath());
    }

    public static BaseNodeExecutor buildExecutor(String fileName) throws ScriptException {
        return buildExecutor("",fileName);
    }
    public static BaseNodeExecutor buildExecutor(String dir,String fileName) throws ScriptException {
        NodeInstance instance = build(dir,fileName + ".json");
        log.info("instance: {}",instance);
        ScriptExecutorManager.setPath("target/classes", "target/classes");
        log.info("classpath: {}",ScriptExecutorManager.getClassPath());

        BaseNodeExecutor nodeExecutor = NodeExecutorFactory.compileBuild(fileName,instance);;

        return nodeExecutor;
    }


    public static NodeInstance build(String fileName) {
        return build("",fileName);
    }
    public static NodeInstance build(String dir,String fileName) {
        if(!fileName.endsWith("json")){
            fileName += ".json";
        }
        String uri = "/node_test/" + dir + "/" + fileName;
        NodeInstance instance = null;
        try {
            URL url = BaseNodeTest.class.getResource(uri);
            if (url == null) {
                throw new RuntimeException("文件不存在：" + url);
            }
            Path p = Paths.get(url.toURI());
            log.info("node json file:{}",p);
            byte[] bytes = Files.readAllBytes(p);

            instance = JSONUtils.parseObject(bytes, NodeInstance.class);
        } catch (URISyntaxException | IOException e) {
            e.printStackTrace();
            return null;
        }

        return instance;
    }
}