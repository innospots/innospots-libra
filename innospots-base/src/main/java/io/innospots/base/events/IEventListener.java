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

package io.innospots.base.events;

import cn.hutool.core.util.TypeUtil;

import javax.annotation.PostConstruct;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;

/**
 * @author Raydian
 * @date 2020/12/14
 */

public interface IEventListener<T extends EventBody> {


    @PostConstruct
    default void register() {
        EventBusCenter.getInstance().register(this);
    }

    /**
     * event fire listener execute
     *
     * @param event
     * @return
     */
    Object listen(T event);

    /**
     * @return
     */
    default Class<T> eventBodyClass(){
        Type[] interfaces = getClass().getGenericInterfaces();
        Type type = ((ParameterizedType) interfaces[0]).getActualTypeArguments()[0];
        return (Class<T>) type;
    }

    default String name() {
        return this.getClass().getSimpleName();
    }

}
