package io.innospots.workflow.core.node.builder;

import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.base.script.jit.JavaSourceFileCompiler;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/7/17
 */
@Slf4j
public class NodeDefinitionBuilder {

    private String sourceRootPath;

    private String nodeClassPath;

    private String defaultImport = "import io.innospots.workflow.node.autogen;";

    private Path targetClassPath() {
        return Path.of(ScriptExecutorManager.getClassPath());
    }

    public void build(NodeDefinition nodeDefinition) {
        String source = nodeDefinition.scriptAction();
        if (source == null) {
            return;
        }
        JavaSourceFileCompiler javaSourceFileCompiler = new JavaSourceFileCompiler(targetClassPath());
        source = defaultImport + "\n\n" + source;
        Path sourcePath = Path.of(ScriptExecutorManager.getSourcePath(),"src_node",getClassName(source)+".java");

        try {
            Files.write(sourcePath, source.getBytes(), StandardOpenOption.CREATE,StandardOpenOption.WRITE);
        } catch (IOException e) {
            log.error(e.getMessage(),e);
        }
        javaSourceFileCompiler.addSourceFile(sourcePath.toFile());
        try {
            javaSourceFileCompiler.compile();
        } catch (IOException e) {
            log.error(e.getMessage(), e);
        }
    }

    private String getClassName(String source) {
        String prefix = "public class ";
        int p = source.indexOf(prefix);
        if (p < 0) {
            return null;
        }
        int e = source.indexOf(" ",p+1);
        String className = source.substring(p+prefix.length(),e);

        return className.trim();
    }

}
