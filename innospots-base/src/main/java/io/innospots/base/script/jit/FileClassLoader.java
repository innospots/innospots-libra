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

import com.github.benmanes.caffeine.cache.CacheLoader;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.checkerframework.checker.nullness.qual.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URL;
import java.net.URLClassLoader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.concurrent.TimeUnit;

/**
 * 缓存的classPath路径
 *
 * @author Smars
 * @date 2021/8/4
 */
public class FileClassLoader extends URLClassLoader {

    private static final Logger logger = LoggerFactory.getLogger(FileClassLoader.class);

    private Path classPath;

    private LoadingCache<String, Class<?>> classCache = Caffeine.newBuilder()
            .expireAfterAccess(60, TimeUnit.MINUTES)
            .build(new CacheLoader<String, Class<?>>() {
                @Nullable
                @Override
                public Class<?> load(@NonNull String s) throws Exception {
                    return findClass(s);
                }
            });

    public FileClassLoader(ClassLoader parent, Path classPath) {
        super(new URL[0], parent);
        this.classPath = classPath;
    }

    public void clear(String name) {
        classCache.invalidate(name);
    }

    public FileClassLoader(Path classPath) {
        super(new URL[0], FileClassLoader.class.getClassLoader());
        this.classPath = classPath;
    }

    @Override
    public Class<?> loadClass(String name) throws ClassNotFoundException {
        if (!name.startsWith("live.re")) {
            return Class.forName(name);
//            return super.findClass(name);
        }
        Class<?> clazz = classCache.get(name);
        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }
        return clazz;
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        if (!name.startsWith("live.re")) {
            return super.findClass(name);
        }
        String[] clPath = name.split("\\.");
        clPath[clPath.length - 1] = clPath[clPath.length - 1] + ".class";

        Path classFile = Paths.get(classPath.toAbsolutePath().toString(), clPath);

        if (!classFile.toFile().exists()) {
            throw new ClassNotFoundException(classPath.toString());
        }
        Class<?> clazz = null;
        try {
            byte[] classBytes = Files.readAllBytes(classFile);
            clazz = defineClass(name, classBytes, 0, classBytes.length);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        if (clazz == null) {
            throw new ClassNotFoundException(name);
        }

        return clazz;
    }
}
