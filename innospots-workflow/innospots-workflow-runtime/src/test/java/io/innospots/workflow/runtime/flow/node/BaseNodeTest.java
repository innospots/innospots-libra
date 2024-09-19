package io.innospots.workflow.runtime.flow.node;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.NodeExecutorFactory;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.MapUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;

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

        BaseNodeExecutor nodeExecutor = NodeExecutorFactory.compileBuild(fileName,instance);

        return nodeExecutor;
    }

    public static void output(NodeExecution nodeExecution){
        for (ExecutionOutput output : nodeExecution.getOutputs()) {
            System.out.println(output.log());
            for (Map<String, Object> result : output.getResults()) {
                System.out.println(result);
            }
            if(MapUtils.isNotEmpty(output.getResources())){
                output.getResources().forEach((k,v)->{
                    for (ExecutionResource executionResource : v) {
                        System.out.println(executionResource.toMetaInfo());
                    }
                });
            }
        }
    }

    public static BaseNodeExecutor buildExecutor(NodeInstance nodeInstance) {
        ScriptExecutorManager.setPath("target/classes", "target/classes");
        return NodeExecutorFactory.compileBuild(nodeInstance.getNodeKey(),nodeInstance);
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
                throw new RuntimeException("文件不存在：" + uri);
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