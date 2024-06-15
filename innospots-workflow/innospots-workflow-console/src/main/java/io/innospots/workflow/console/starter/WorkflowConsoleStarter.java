package io.innospots.workflow.console.starter;

import io.innospots.workflow.core.node.definition.meta.NodeMetaInfoLoader;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/15
 */
@Component
public class WorkflowConsoleStarter implements ApplicationRunner {

    @Override
    public void run(ApplicationArguments args) throws Exception {
        NodeMetaInfoLoader.load();
    }
}
