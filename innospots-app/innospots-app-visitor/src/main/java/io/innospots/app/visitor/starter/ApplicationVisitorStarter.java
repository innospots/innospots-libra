package io.innospots.app.visitor.starter;

import io.innospots.app.visitor.config.AppVisitorProperties;
import io.innospots.base.constant.ServiceConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/1
 */
@Slf4j
@Component
public class ApplicationVisitorStarter implements ApplicationRunner {

    private AppVisitorProperties appVisitorProperties;

    public static final String KEY_WORKFLOW_API = "workflow.address";
    public static final String KEY_WORKFLOW_TEST_API = "workflow.test.address";

    public ApplicationVisitorStarter(AppVisitorProperties appVisitorProperties) {
        this.appVisitorProperties = appVisitorProperties;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {

        System.setProperty(KEY_WORKFLOW_API, ServiceConstant.webHookApi(appVisitorProperties.getWorkflowAddress()));
        System.setProperty(KEY_WORKFLOW_TEST_API, ServiceConstant.webHookApiTest(appVisitorProperties.getWorkflowTestAddress()));
        log.info("workflow api: {}", System.getProperty(KEY_WORKFLOW_API));
        log.info("workflow test api: {}", System.getProperty(KEY_WORKFLOW_TEST_API));
    }

}
