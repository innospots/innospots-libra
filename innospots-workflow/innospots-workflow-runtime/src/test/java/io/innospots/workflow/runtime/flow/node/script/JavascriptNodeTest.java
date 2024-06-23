package io.innospots.workflow.runtime.flow.node.script;

import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
public class JavascriptNodeTest implements IDataNodeTest {

    @Test
    void test() {
        this.testExecute(5);
    }

    @Override
    public NodeInstance build() {
        return BaseNodeTest.build("script","JavascriptNodeTest");
    }
}
