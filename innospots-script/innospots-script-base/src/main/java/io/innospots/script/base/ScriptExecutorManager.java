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

package io.innospots.script.base;

import cn.hutool.core.lang.ClassScanner;
import io.innospots.base.enums.ScriptType;
import io.innospots.base.exception.ScriptException;
import io.innospots.script.base.jit.JavaSourceFileCompiler;
import io.innospots.script.base.jit.JavaSourceFileStaticBuilder;
import io.innospots.script.base.jit.MethodBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;

/**
 * script executor manager
 *
 * @author Smars
 * @date 2021/5/16
 */
public class ScriptExecutorManager {

    private static final Logger logger = LoggerFactory.getLogger(ScriptExecutorManager.class);

    public static final String DEFAULT_EXP_PKG = "autogen";

    public static final String CLASS_PATH_ENV = "live.classpath";
    public static final String SOURCE_PATH_EVN = "live.sourcepath";
    public static final String SCRIPT_PACKAGES = "script.packages";

    /**
     * source path
     */
    protected Path sourcePath;

    protected Path classPath;

    protected String identifier;


    protected JavaSourceFileStaticBuilder sourceBuilder;

    protected String packageName;


    private static boolean retainSource = true;

    protected Map<String, IScriptExecutor> executors = new HashMap<>();

    /**
     * spi loader, key: scriptType, value: class
     */
    private static Map<String, Class<? extends IScriptExecutor>> executorClasses = new HashMap<>();

    static {
        initialize();
    }

    private static void initialize(){
        String scriptPackages = System.getProperty(SCRIPT_PACKAGES,"io.innospots");
        Set<Class<?>> executorCls = ClassScanner.scanPackageBySuper(scriptPackages,IScriptExecutor.class);

        for (Class<?> executorCl : executorCls) {
            if(!Modifier.isInterface(executorCl.getModifiers()) && !Modifier.isAbstract(executorCl.getModifiers())){
                try {
                    IScriptExecutor scriptExecutor = (IScriptExecutor) executorCl.getConstructor().newInstance();
                    logger.info("load script scriptType:{}, suffix:{}, executeMode:{}, class:{}",scriptExecutor.scriptType(),scriptExecutor.suffix(),scriptExecutor.executeMode(),executorCl.getName());
                    executorClasses.put(scriptExecutor.scriptType(), (Class<? extends IScriptExecutor>) executorCl);
                } catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                         NoSuchMethodException e) {
                    logger.error(e.getMessage(),e);
                }
            }
        }
        logger.info("load script executor size:{}",executorClasses.size());
    }

    public static ScriptExecutorManager newInstance(String identifier) {
        return new ScriptExecutorManager(identifier);
    }

    public ScriptExecutorManager(String identifier) {
        this.identifier = identifier;
        this.packageName = DEFAULT_EXP_PKG + "." + identifier.toLowerCase();
        sourceBuilder();
    }

    public synchronized void register(MethodBody methodBody) {
        if (methodBody.getScriptType() == null) {
            logger.warn("script type is null, method:{} ", methodBody);
            return;
        }
        IScriptExecutor executor = newScriptExecutor(methodBody.getScriptType());
        methodBody.setSuffix(executor.suffix());
        executor.reBuildMethodBody(methodBody);
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
                default:
        }
        executors.put(methodBody.getMethodName(),executor);
    }

    public boolean build() throws ScriptException {
        JavaSourceFileCompiler compiler = new JavaSourceFileCompiler(classPath());
        if (this.sourceBuilder.hasSourceBody()) {
            try {
                logger.info("compile engine:{}, write source file:{}", className(), sourceBuilder.getSourceFile().getAbsolutePath());
                this.sourceBuilder.writeToFile();
            } catch (IOException e) {
                throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA.name(), e, e.getMessage());
            }
        }

        if (sourceBuilder.sourceFileExists()) {
            compiler.addSourceFile(sourceBuilder.getSourceFile());
            try {
                compiler.compile();
                //reload();
                sourceBuilder.clear();
                if(!retainSource){
                    sourceBuilder.deleteSourceFile();
                }
                logger.info("engine class file write complete, classFile:{} , loaded executor size:{}", className(), executors.size());
                reload();
                return true;
            } catch (IOException e) {
                throw ScriptException.buildCompileException(this.getClass(), ScriptType.JAVA.name(), e, e.getMessage());
            }
        }

        return false;
    }

    public void reload() throws ScriptException {
        try {
            Class<?> clazz = classForName();
            for (Method method : clazz.getDeclaredMethods()) {
                String methodName = method.getName();
                if (methodName.startsWith("_")) {
                    int f = methodName.indexOf("$");
                    if(f > 0){
                        methodName = methodName.substring(f);
                    }
                }
                IScriptExecutor executor = this.executors.get(methodName);
                if(executor!=null){
                    executor.initialize(method);
                }else{
                    logger.error("script executor not be defined: {}", methodName);
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

    public static void setRetainSource(boolean retainSource){
        ScriptExecutorManager.retainSource = retainSource;
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
    public void clear() {
        File clsFile = new File(classPath().toFile(), className().replace(".", File.separator) + ".class");
        if (clsFile.exists()) {
            logger.debug("remove class file:{}", clsFile.getPath());
            clsFile.delete();
        }
        sourceBuilder.deleteSourceFile();
    }

    public String identifier() {
        return identifier;
    }

    public String className() {
        return packageName + "." + identifier;
    }

    public IScriptExecutor getExecutor(String methodName) {
        if (this.executors != null) {
            return this.executors.get(methodName);
        }
        return null;
    }

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
        Class<? extends IScriptExecutor> seClass = executorClasses.get(scriptType);
        try {
            if(seClass == null){
                throw ScriptException.buildCompileException(this.getClass(),scriptType,"script executor is null,scriptType:" + scriptType);
            }
            return seClass.getConstructor().newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }
    }


}
