package io.innospots.workflow.runtime.flow.node.file;

import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.runtime.flow.node.BaseNodeTest;
import io.innospots.workflow.runtime.flow.node.IDataNodeTest;
import org.junit.jupiter.api.Test;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/23
 */
public class ExcelFileNodeTest implements IDataNodeTest {

    @Test
    void test1(){
        EncryptorBuilder.initialize("abc");
        this.testExecute(10);
    }


    @Override
    public NodeInstance build() {
        return BaseNodeTest.build("file",ExcelFileNodeTest.class.getSimpleName());
    }
}
