package io.innospots.workflow.node.app.file;

import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.execution.model.node.NodeOutput;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.BaseNodeTest;
import io.innospots.workflow.node.app.NodeExecutionTest;
import org.junit.jupiter.api.Test;

import java.util.List;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/25
 */
class OutputFilesNodeTest {


    @Test
    void testInstance(){
        NodeInstance nodeInstance = BaseNodeTest.build(OutputFilesNodeTest.class.getSimpleName()+".json");
        System.out.println(nodeInstance);
        System.out.println(System.getenv("HOME"));
        BaseNodeExecutor appNode = BaseNodeTest.baseAppNode(OutputFilesNodeTest.class.getSimpleName());
        NodeExecution ne2 = LoadFilesNodeTest.readExecution();
        NodeExecution nodeExecution = NodeExecutionTest.build("key12345");

        ExecutionInput executionInput = new ExecutionInput();
        for (NodeOutput output : ne2.getOutputs()) {
            for (List<ExecutionResource> value : output.getResources().values()) {
                executionInput.addResource(value);
            }
        }//end for

        nodeExecution.addInput(executionInput);

        appNode.invoke(nodeExecution);
        System.out.println(NodeExecutionDisplay.build(nodeExecution,null));
    }

    @Test
    void testFile() {

    }

}