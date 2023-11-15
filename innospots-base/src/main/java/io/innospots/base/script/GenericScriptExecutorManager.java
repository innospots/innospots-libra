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

package io.innospots.base.script;

import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.base.script.aviator.AviatorScriptScriptExecutorManager;
import io.innospots.base.script.java.JavaScriptExecutor;
import io.innospots.base.script.javascript.JavaScriptScriptExecutorManager;
import io.innospots.base.script.jit.FileClassLoader;
import io.innospots.base.script.jit.JavaSourceFileCompiler;
import io.innospots.base.script.jit.JavaSourceFileStaticBuilder;
import io.innospots.base.script.jit.MethodBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

/**
 * 通用表达式引擎，底层使用JVM做基础构建引擎，兼容其他脚本在jvm的执行
 *
 * @author Smars
 * @date 2021/5/16
 */
public class GenericScriptExecutorManager implements IScriptExecutorManager {

    private static final Logger logger = LoggerFactory.getLogger(GenericScriptExecutorManager.class);

    public static final String DEFAULT_EXP_PKG = "live.re.scripts";

    public static final String CLASS_PATH_ENV = "live.classpath";
    public static final String SOURCE_PATH_EVN = "live.sourcepath";

    /**
     * source path
     */
    protected Path sourcePath;

    protected Path classPath;

    protected String identifier;


    protected JavaSourceFileStaticBuilder sourceBuilder;

    protected String packageName;

    protected Map<String, IScriptExecutor> executors;

    /**
     * spi loader, key: scriptType, value: class
     */
    private static Map<String, Class<IScriptExecutor>> executorClasses = new HashMap<>();

//    protected FileClassLoader classLoader;


    public static GenericScriptExecutorManager newInstance(String identifier) {
        return new GenericScriptExecutorManager(identifier);
    }

    public GenericScriptExecutorManager(String identifier) {
        this.identifier = identifier;
        sourceBuilder();
    }


    @Override
    public synchronized void register(MethodBody methodBody) {
        if (methodBody.getScriptType() == null) {
            logger.warn("script type is null, method:{} ", methodBody);
            return;
        }
        IScriptExecutor executor = newScriptExecutor(methodBody.getScriptType());
        methodBody.setSuffix(executor.suffix());
        switch (executor.executeMode()){
            case NATIVE:
                this.sourceBuilder.addMethod(methodBody);
                break;
            case CMD:
                this.sourceBuilder.addCmdMethod(methodBody);
                break;
            case SCRIPT:
                this.sourceBuilder.addScriptMethod(methodBody);
                break;
        }
        executors.put(methodBody.getMethodName(),executor);
    }



    @Override
    public boolean build() throws ScriptException {
        JavaSourceFileCompiler compiler = new JavaSourceFileCompiler(classPath());
        if (this.sourceBuilder.hasSourceBody()) {
            try {
                logger.info("compile engine:{}, write source file:{}", className(), sourceBuilder.getSourceFile());
                this.sourceBuilder.writeToFile();
            } catch (IOException e) {
                throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA, e, e.getMessage());
            }
        }

        if (sourceBuilder.sourceFileExists()) {
            compiler.addSourceFile(sourceBuilder.getSourceFile());
            try {
                compiler.compile();
                reload();
                logger.info("engine class file write complete, classFile:{} , loaded expresion size:{}", className(), executors.size());
                return true;
            } catch (IOException e) {
                throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA, e, e.getMessage());
            }
        }

        return false;
    }


    @Override
    public void reload() throws ScriptException {
        try {
            Class<?> clazz = classForName();
            for (Method method : clazz.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("_")) {
                    String[] ms = methodName.split("_");
                    methodName = ms[ms.length-1];
                }
                IScriptExecutor executor = this.executors.get(methodName);
                if(executor!=null){
                    executor.initialize(method);
                }else{
                    logger.warn("script executor not be defined: {}", methodName);
                }
            }
            logger.debug("executorManager:{} , loaded executor size:{}", className(), executors.size());
        } catch (ClassNotFoundException | MalformedURLException e) {
            logger.warn("executorManager:{}  reload executor:{}", className(), e.getMessage());
        }
    }




    public static void setPath(String sourcePath, String classPath) {
        System.setProperty(CLASS_PATH_ENV, classPath);
        System.setProperty(SOURCE_PATH_EVN, sourcePath);
    }

    public static String getClassPath() {
        return System.getProperty(CLASS_PATH_ENV);
    }

    public static String getSourcePath() {
        return System.getProperty(SOURCE_PATH_EVN);
    }

    /**
     * 删除class文件
     *
     * @return
     */
    @Override
    public void clear() {
        File clsFile = new File(classPath().toFile(), className().replace(".", File.separator) + ".class");
        logger.debug("remove class file:{}", clsFile.getPath());
        if (clsFile.exists()) {
            clsFile.delete();
        }
        sourceBuilder().deleteSourceFile();
        /*
        File sourceFile = new File(sourcePath().toFile(),identifier+".java");
        logger.debug("remove source file:{}",sourceFile);
        if(sourceFile.exists()){
            sourceFile.delete();
        }

         */
    }

    @Override
    public String identifier() {
        return identifier;
    }

    public String className() {
        return packageName + "." + identifier;
    }

    @Override
    public IScriptExecutor getExecutor(String methodName) {
        if (this.executors != null) {
            return this.executors.get(methodName);
        }
        return null;
    }

    @Override
    public boolean isLoaded() {
        return this.executors != null;
    }


    public Class<?> classForName() throws ClassNotFoundException, MalformedURLException {
        classPath();
        return classPath == null ?
                Class.forName(className()) :
                Class.forName(className(), true, new URLClassLoader(new URL[]{classPath.toUri().toURL()}));
    }


    private Path sourcePath() {
        if (this.sourcePath != null) {
            return sourcePath;
        }
        String path = getSourcePath();
        if (path == null) {
            classPath();
            sourcePath = Paths.get(classPath.toAbsolutePath().toString(), "src");
            logger.warn("system variable has not been set: live.sourcepath, please setting variable: System.setProperty(SOURCEPATH_ENV,path)");
        } else {
            sourcePath = Paths.get(path);
        }
        File clzDir = sourcePath.toFile();
        if (!clzDir.exists()) {
            clzDir.mkdirs();
        }
        return sourcePath;
    }

    private Path classPath() {
        if (this.classPath != null) {
            return classPath;
        }
        String path = getClassPath();
        if (path == null) {
            classPath = Paths.get("");
            logger.warn("system variable has not been set: live.sourcepath, please setting variable: System.setProperty(CLASSPATH_ENV,path)");
        } else {
            classPath = Paths.get(path);
        }
        File clzDir = classPath.toFile();
        if (!clzDir.exists()) {
            clzDir.mkdirs();
        }
        return classPath;
    }

    /*
    protected FileClassLoader classLoader(boolean update) {
        if (this.classLoader == null || update) {
            this.classLoader = new FileClassLoader(classPath());
        }
        return new FileClassLoader(classPath);
    }

     */


    private JavaSourceFileStaticBuilder sourceBuilder() {
        if (sourceBuilder == null) {
            try {
                sourceBuilder = JavaSourceFileStaticBuilder.newBuilder(identifier, packageName, sourcePath());
            } catch (Exception e) {
                logger.warn(e.getMessage());
            }
        }//end if
        return sourceBuilder;
    }

    private IScriptExecutor newScriptExecutor(String scriptType) {
        Class<IScriptExecutor> seClass = executorClasses.get(scriptType);
        try {
            return seClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


}
