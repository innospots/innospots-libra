package io.innospots.workflow.runtime.flow.load;

import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.workflow.core.flow.Flow;
import io.innospots.workflow.core.flow.loader.FsWorkflowLoader;
import io.innospots.workflow.core.flow.model.BuildProcessInfo;
import io.innospots.workflow.core.flow.model.WorkflowBody;
import io.innospots.workflow.core.instance.model.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
@Slf4j
public class FlowLoadTest {

    @Test
    void testLoad() throws URISyntaxException, IOException {
        ScriptExecutorManager.setPath("target/classes", "target/classes");
        String uri = "/flow/flow_sample.json";
        WorkflowBody body =  build(uri);
        Flow flow = new Flow(body,true);
        BuildProcessInfo processInfo = flow.prepare();

    }

    public static WorkflowBody build(String filename) throws URISyntaxException, IOException {
        String uri = "/flow/flow_sample.json";
        URL url = FlowLoadTest.class.getResource(uri);
        Path p = Paths.get(url.toURI());
        log.info("flow json file:{}",p);
        byte[] bytes = Files.readAllBytes(p);
        WorkflowBody workflowBody = JSONUtils.parseObject(bytes, WorkflowBody.class);
        workflowBody.initialize();
        System.out.println(workflowBody);
        for (NodeInstance node : workflowBody.getNodes()) {
            System.out.println(node);
        }
        return workflowBody;
    }

}
