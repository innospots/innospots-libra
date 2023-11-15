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

package io.innospots.base.script.cmdline;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.SourceFileBuilder;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/11/7
 */
@Slf4j
public class SimpleScriptSourceFileBuilder implements SourceFileBuilder {

    private File sourceDirectory;

    private String suffix;
    private Map<String, String> scripts = new LinkedHashMap<>();

    public SimpleScriptSourceFileBuilder(String scriptPath, String identifier, String scriptType, String suffix) {
        sourceDirectory = Paths.get(scriptPath, identifier, scriptType).toFile();
        if (!sourceDirectory.exists()) {
            sourceDirectory.mkdirs();
        }
        this.suffix = suffix;

    }

//    @Override
//    public void addMethod(Class<?> returnType, String body, String methodName, ParamField... params) {
//        scripts.put(methodName, body);
//    }

    @Override
    public void writeToFile() throws IOException {
        if (!sourceDirectory.exists()) {
            sourceDirectory.mkdirs();
        }
        for (Map.Entry<String, String> sourceEntry : scripts.entrySet()) {
            File scriptFile = new File(sourceDirectory, sourceEntry.getKey() + "." + suffix);
            log.info("write source file:{}", scriptFile.getAbsolutePath());
            if (scriptFile.exists()) {
                scriptFile.delete();
            }
            Files.write(scriptFile.toPath(), sourceEntry.getValue().getBytes());
        }
    }

    public boolean hasSourceBody() {
        return !scripts.isEmpty();
    }

    @Override
    public String toSource() {
        return "";
    }

    @Override
    public void clear() {
        scripts.clear();
    }

    @Override
    public void deleteSourceFile() {
        for (File scriptFile : getScriptFiles()) {
            scriptFile.delete();
        }
        sourceDirectory.delete();
    }

    public File[] getScriptFiles() {
        return sourceDirectory.listFiles((dir, name) -> name.endsWith(suffix));
    }

    public File sourceDirectory() {
        return this.sourceDirectory;
    }
}
