package io.innospots.workflow.core.node.builder;

import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.ScriptExecutorManager;
import io.innospots.base.script.jit.JavaSourceFileCompiler;
import io.innospots.workflow.core.node.definition.model.NodeDefinition;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/7/17
 */
@Slf4j
public class NodeDefinitionCodeBuilder {

    private static final String defaultPackage = "io.innospots.workflow.node.autogen";

    private static Path targetClassPath() {
        return Path.of(ScriptExecutorManager.getClassPath());
    }

    public static boolean build(NodeDefinition nodeDefinition) {
        String source = nodeDefinition.scriptAction();
        if (source == null || source.trim().startsWith("class:")) {
            return true;
        }
        JavaSourceFileCompiler javaSourceFileCompiler = new JavaSourceFileCompiler(targetClassPath());
        source = "package "+defaultPackage + ";\n\n" + source;
        String className = getClassName(source);
        Path sourcePath = Path.of(
                new File(ScriptExecutorManager.getSourcePath()).getAbsolutePath(),
                "src_node",className+".java");

        try {
            Files.createDirectories(sourcePath.getParent());
            Files.write(sourcePath, source.getBytes(),
                    StandardOpenOption.CREATE,
                    StandardOpenOption.TRUNCATE_EXISTING,
                    StandardOpenOption.WRITE);
            javaSourceFileCompiler.addSourceFile(sourcePath.toFile());
            javaSourceFileCompiler.compile();
            Class<?> nodeClass = Class.forName(defaultPackage+"."+className,true,
                    new URLClassLoader(new URL[]{targetClassPath().toUri().toURL()}));
            nodeDefinition.setNodeType(nodeClass.getName());
        } catch (IOException | ClassNotFoundException e) {
            log.error(e.getMessage(),e);
            throw ScriptException.buildCompileException(NodeDefinitionCodeBuilder.class,"JAVA","node definition script compile is failed.",e.getMessage());
        }

        return true;
    }

    private static String getClassName(String source) {
        String prefix = "public class ";
        int p = source.indexOf(prefix);
        if (p < 0) {
            return null;
        }
        p = p+prefix.length();
        int e = source.indexOf(" ",p+1);
        String className = source.substring(p,e);

        return className.trim();
    }

}
