package io.innospots.workflow.node.app.file;

import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.execution.model.node.NodeExecutionDisplay;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.BaseNodeTest;
import io.innospots.workflow.node.app.NodeExecutionTest;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/25
 */
class LoadFilesNodeTest {


    @Test
    void testInstance(){
        NodeExecution nodeExecution = readExecution();
        System.out.println(NodeExecutionDisplay.build(nodeExecution,null));
    }

    static NodeExecution readExecution() {
        NodeInstance nodeInstance = BaseNodeTest.build(LoadFilesNodeTest.class.getSimpleName()+".json");
        System.out.println(nodeInstance);
        System.out.println(System.getenv("HOME"));
        BaseNodeExecutor appNode = BaseNodeTest.baseAppNode(LoadFilesNodeTest.class.getSimpleName());
        NodeExecution nodeExecution = NodeExecutionTest.build("key12345");
        appNode.invoke(nodeExecution);
        return nodeExecution;
    }

    @Test
    void testFile() {
//        String imgDir = "/tmp/abbc/*.img";
        String imgDir = "/tmp";
        System.out.println(new File("/tmp/a.img").isFile());
        System.out.println(new File("/tmp/*.img").getName());
        String pt = new File("/tmp/*").getName().replace(".", "\\.").replace("*", ".*");
        System.out.println(pt);
        File fs = new File(imgDir);
        File[] ffs = fs.listFiles(new FilenameFilter() {
            @Override
            public boolean accept(File dir, String name) {
                return name.matches(pt);
            }
        });
        System.out.println(Arrays.toString(ffs));
        System.out.println(fs.isFile());
        System.out.println(fs.isDirectory());
        System.out.println(fs.getAbsolutePath());
        System.out.println(fs.getParent());
        System.out.println(fs.getParentFile().getAbsolutePath());
        LoadFilesNode startNode = new LoadFilesNode();
        //File[] ff =  startNode.selectFiles(imgDir);
    }

    @Test
    void testSelectFiles() {
        LoadFilesNode loadFilesNode = new LoadFilesNode();
        File[] files = loadFilesNode.selectFiles("/tmp/*.img");
        System.out.println(Arrays.toString(files));
        files = loadFilesNode.selectFiles("/tmp/*");
        System.out.println(Arrays.toString(files));
        files = loadFilesNode.selectFiles(System.getProperty("user.home") + "/Downloads/*.pdf");
        System.out.println(Arrays.toString(files));
        System.getProperties().forEach((k, v) -> {
            System.out.println(k + ":" + v);
        });
    }

}