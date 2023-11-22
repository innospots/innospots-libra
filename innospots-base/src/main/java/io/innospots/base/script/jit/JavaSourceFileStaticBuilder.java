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

import cn.hutool.core.io.FileUtil;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.field.ParamField;
import io.innospots.base.script.java.ScriptMeta;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/5/24
 */
@Slf4j
public class JavaSourceFileStaticBuilder {


    private String packageName;

    private String className;

    private Map<String, String> methods = new HashMap<>();

    private Set<String> imports = new LinkedHashSet<>();

    private Set<String> varFields = new LinkedHashSet<>();

    private File sourceFile;

//    private String identifier;

    private File scriptDir;

//    private String sourcePath;


    public static JavaSourceFileStaticBuilder newBuilder(String className, String packageName, Path sourcePath) {
        JavaSourceFileStaticBuilder builder = new JavaSourceFileStaticBuilder();
        builder.className(className).packageName(packageName).initialize();
        File parentDir = new File(sourcePath.toFile().getAbsolutePath());
        builder.scriptDir = new File(parentDir.getParentFile(), packageName.replace(".", File.separator));
        if (!builder.scriptDir.exists()) {
            builder.scriptDir.mkdirs();
        }
        builder.sourceFile = new File(sourcePath.toFile(), className + ".java");
        return builder;
    }

    private JavaSourceFileStaticBuilder initialize() {
        this.addImport("import io.innospots.base.script.java.ParamName;")
                .addImport("import io.innospots.base.script.java.ScriptMeta;")
                .addImport("import io.innospots.base.json.JSONUtils;")
                .addImport("import java.util.*;")
                .addImport("import java.util.regex.*;")
                .addImport("import java.util.function.*;")
                .addImport("import java.util.concurrent.*;")
                .addImport("import java.util.concurrent.atomic.*;")
                .addImport("import java.time.*;")
                .addImport("import java.text.*;")
                .addImport("import org.slf4j.Logger;")
                .addImport("import org.slf4j.LoggerFactory;")
                .addImport("import com.googlecode.aviator.AviatorEvaluator;")
                .addImport("import com.googlecode.aviator.Expression;")
                .addImport("import org.slf4j.LoggerFactory;")
                .addImport("import static io.innospots.base.script.function.HttpFunc.*;")
                .addImport("import static io.innospots.base.utils.time.DateTimeUtils.*;")
                .addField("private static final Logger logger = LoggerFactory.getLogger(" + className + ".class);");
        return this;
    }

    JavaSourceFileStaticBuilder packageName(String packageName) {
        this.packageName = packageName;
        return this;
    }

    JavaSourceFileStaticBuilder className(String className) {
        this.className = className;
        return this;
    }


    private void addMethod(String methodName, String srcMethod) {
        this.methods.put(methodName, srcMethod);
    }

    public void addCmdMethod(MethodBody methodBody) {
        this.addScriptMethod(methodBody);
    }

    /**
     * 脚本方法
     *
     * @param methodBody
     * @return
     */
    public void addScriptMethod(MethodBody methodBody) {
        StringBuilder buf = new StringBuilder();
        fillMeta(buf, methodBody);
        buf.append("public static String");
        buf.append(" _");
        buf.append(methodBody.getScriptType());
        buf.append("_");
        buf.append(methodBody.getSuffix());
        buf.append("_");
        buf.append(methodBody.getMethodName());
        buf.append("() {\n");
        buf.append("String script =\"");
        buf.append(methodBody.getSrcBody());
        buf.append("\";");
        buf.append("\nreturn script;");
        buf.append("}");
        addMethod(methodBody.getMethodName(), buf.toString());
    }

    private void fillMeta(StringBuilder buf, MethodBody methodBody) {
        buf.append("@ScriptMeta(scriptType=\"").append(methodBody.getScriptType()).append("\"");
        buf.append(" ,suffix=\"").append(methodBody.getSuffix()).append("\"");
        if (methodBody.getParams() != null) {
            String args = methodBody.getParams().stream().map(f -> {
                        String arg = "";
                        FieldValueType valueType = f.getValueType();
                        if (valueType == null) {
                            arg = Object.class.getSimpleName();
                        } else {
                            arg = valueType.getClazz().getName();
                        }
                        return "\"" + arg + " " + f.getCode() + "\"";
                    })
                    .collect(Collectors.joining(","));

            buf.append(" ,args={").append(args).append("}");
        }
        buf.append(" ,path=\"").append(scriptDir.getPath()).append(File.separator).append(methodBody.scriptName()).append("\"");
        buf.append(", returnType=").append(methodBody.getReturnType().getSimpleName()).append(".class");
        buf.append(")\n");
    }

    public void addMethod(MethodBody methodBody) {
        if (CollectionUtils.isEmpty(methodBody.getParams())) {
            ParamField paramField = new ParamField();
            paramField.setCode("item");
            paramField.setName("item");
            paramField.setValueType(FieldValueType.MAP);
            paramField.setComment("inputParam");
            List<ParamField> pm = new ArrayList<>();
            pm.add(paramField);
            methodBody.setParams(pm);
        }
        StringBuilder buf = new StringBuilder();
        fillMeta(buf, methodBody);
        buf.append("public static ");
        Class<?> returnType = methodBody.getReturnType();
        String methodName = methodBody.getMethodName();
        ParamField[] params = methodBody.getParams().toArray(new ParamField[0]);
        String body = methodBody.getSrcBody();

        if (returnType == null || returnType.equals(Void.class)) {
            buf.append("void");
        } else {
            buf.append(returnType.getName());
        }
        buf.append(" ");
        buf.append(methodName);
        buf.append("(");
        for (int i = 0; i < params.length; i++) {
            buf.append("@ParamName(value=\"");
            buf.append(params[i].getCode());
            buf.append("\") ");
            buf.append(params[i].getValueType().getClazz().getName());
            buf.append(" ");
            buf.append(params[i].getCode());
            if (i < params.length - 1) {
                buf.append(", ");
            }
        }//end for

        buf.append(") {");
        buf.append("\n");
        buf.append(tab(4));
        buf.append("try {\n");
        buf.append(tab(6));
        buf.append(body);
        buf.append("\n");
        buf.append(tab(4));
        buf.append("}catch (Exception e){\n");
        buf.append(tab(6));
        buf.append("logger.error(e.getMessage(),e);\n");
        buf.append(tab(4));
        buf.append("}\n");

        if (returnType != null && !returnType.equals(Void.class)) {
            buf.append(tab(4)).append("return ");
            if (returnType.equals(Integer.class) ||
                    returnType.equals(Long.class) ||
                    returnType.equals(Float.class) ||
                    returnType.equals(Double.class) ||
                    returnType.equals(Byte.class)
            ) {
                buf.append("0");
            } else if (returnType.equals(BigDecimal.class)) {
                buf.append("BigDecimal.ZERO");
            } else if (returnType.equals(Boolean.class)) {
                buf.append("false");
            } else {
                buf.append("null");
            }
            buf.append(";\n");
        }

        buf.append(tab(2));
        buf.append("}");

        addMethod(methodName, buf.toString());
    }

    public JavaSourceFileStaticBuilder addImport(String importPackage) {
        this.imports.add(importPackage);
        return this;
    }

    public JavaSourceFileStaticBuilder addField(String field) {
        this.varFields.add(field);
        return this;
    }


    public void writeToFile() throws IOException {
        Files.write(sourceFile.toPath(), toSource().getBytes());
    }

    public File getSourceFile() {
        return sourceFile;
    }

    public boolean sourceFileExists() {
        return sourceFile.exists();
    }


    public void deleteSourceFile() {
        log.debug("remove source file:{}", sourceFile);
        if (sourceFile.exists()) {
            sourceFile.delete();
        }
    }

    public boolean hasSourceBody() {
        return !this.methods.isEmpty();
    }

    public String fullName() {
        return packageName + "." + className;
    }

    /**
     * 构造输出类的源码，源码为静态方法的的调用
     *
     * @return
     */
    public String toSource() {
        StringBuilder src = new StringBuilder("package ");
        src.append(packageName);
        src.append(";\n\n");

        if (!imports.isEmpty()) {
            for (String anImport : imports) {
                src.append(anImport);
                src.append("\n");
            }
        }
        src.append("\n@SuppressWarnings(\"unchecked\")");
        src.append("\npublic class ");
        src.append(className);
        src.append(" {\n");

        if (!this.varFields.isEmpty()) {
            src.append("\n");
            for (String varField : this.varFields) {
                src.append(tab(2));
                src.append(varField);
                src.append("\n\n");
            }
        }

        if (!this.methods.isEmpty()) {
            for (String method : this.methods.values()) {
                src.append(tab(2));
                src.append(method);
                src.append("\n\n");
            }
        }

        src.append("}//end class");

        return src.toString();
    }


    public void clear() {
        this.varFields.clear();
        this.methods.clear();
        this.imports.clear();
        this.initialize();
    }


    private String tab(int size) {
        return StringUtils.leftPad("", size);
    }


}
