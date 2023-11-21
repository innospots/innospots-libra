/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.base.script.jit;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Smars
 * @date 2021/5/24
 */
public class JavaSourceFileCompiler {

    private static final Logger logger = LoggerFactory.getLogger(JavaSourceFileCompiler.class);

    private JavaCompiler javaCompiler;

    private Set<File> sourceFiles = new LinkedHashSet<>();
    private Path classTargetDir;

    public JavaSourceFileCompiler(Path classTargetDir) {
        javaCompiler = ToolProvider.getSystemJavaCompiler();
        this.classTargetDir = classTargetDir;
    }

    public void addSourceFile(File sourceFile) {
        this.sourceFiles.add(sourceFile);
    }


    public void compile() throws IOException, ScriptException {

        File target = this.classTargetDir.toFile();
        logger.debug("target dir:{}", target.getAbsolutePath());
        DiagnosticCollector<? super JavaFileObject> diagnostics = new DiagnosticCollector<>();
        StandardJavaFileManager fileManager = javaCompiler.getStandardFileManager(diagnostics, null, null);
        Iterable<? extends JavaFileObject> fileObjects = fileManager.getJavaFileObjectsFromFiles(sourceFiles);
        Iterable<String> options = Arrays.asList("-d", target.getAbsolutePath());
        JavaCompiler.CompilationTask task = javaCompiler.getTask(null, fileManager, diagnostics, options, null,
                fileObjects);
        // 编译源程序
        boolean success = task.call();
        if (!success) {
            logger.error("compile fail source files: {}", sourceFiles);
        }
        StringBuilder errorInfo = new StringBuilder();
        fileManager.close();
        for (Diagnostic object : diagnostics.getDiagnostics()) {
            Diagnostic<? extends JavaFileObject> diagnostic = (Diagnostic) object;
            String err = String.format("Start Position: %s, " + "End Position: %s%n" + "Source: %s%n" + "Message: %s%n",
                    diagnostic.getStartPosition(),
                    diagnostic.getEndPosition(), diagnostic.getSource(), diagnostic.getMessage(null));
            errorInfo.append(err);
            errorInfo.append("\n");
        }
        //update by Wren 20210602 Compile successfully, but there is alarm information
        if (success && errorInfo.length() > 0) {
            logger.warn(errorInfo.toString());
        }
        if (!success) {
            logger.error("java source file compiler error:{}", errorInfo);
            throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA.name(), errorInfo.toString());
        }

    }

}
