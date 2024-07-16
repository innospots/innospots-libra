package io.innospots.workflow.core.node.builder;

import io.innospots.base.script.jit.JavaSourceFileCompiler;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;

import java.nio.file.Path;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/7/17
 */
public class NodeDefinitionBuilder {

    private String sourceRootPath;

    private String nodeClassPath;

    private Path targetClassPath(){
        return null;
    }

    public void build(NodeDefinition nodeDefinition){
        String source = nodeDefinition.scriptAction();
        JavaSourceFileCompiler javaSourceFileCompiler = new JavaSourceFileCompiler(targetClassPath());
    }

}
