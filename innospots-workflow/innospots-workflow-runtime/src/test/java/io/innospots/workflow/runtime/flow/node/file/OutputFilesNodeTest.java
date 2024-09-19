package io.innospots.workflow.runtime.flow.node.file;

import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.workflow.core.config.InnospotsWorkflowProperties;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.file.LoadFilesNode;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import io.innospots.workflow.runtime.flow.node.NodeExecutionTest;
import org.junit.jupiter.api.Test;

import java.io.File;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/25
 */
class OutputFilesNodeTest implements IDataNodeTest {

    @Test
    void testFile() {
        this.testExecute();
    }

    @Override
    public NodeExecution buildExecution(String nodeKey, int size, boolean in) {
        NodeExecution ne = NodeExecutionTest.build(nodeKey);
        File[] files = LoadFilesNode.selectFiles(System.getProperty("user.home")+"/temp/*.pdf");
        ExecutionInput input = new ExecutionInput("abcs");
        EncryptorBuilder.initialize("abc");
        int c = 0;
        for (File rFile : files) {
            ExecutionResource executionResource = ExecutionResource.buildResource(rFile, true, InnospotsWorkflowProperties.WORKFLOW_RESOURCES);
            input.addResource(executionResource);
        }
        ne.addInput(input);
        return ne;
    }

    @Override
    public NodeInstance build() {
        return BaseNodeTest.build("file","OutputFilesNodeTest");
    }
}