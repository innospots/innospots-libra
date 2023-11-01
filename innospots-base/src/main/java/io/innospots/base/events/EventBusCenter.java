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

import io.innospots.base.utils.thread.ThreadPoolBuilder;
import io.innospots.base.utils.thread.ThreadTaskExecutor;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Raydian
 * @date 2020/12/14
 */
@Slf4j
public class EventBusCenter {

    private final HashMap<Class<? extends EventBody>, List<IEventListener<? extends EventBody>>> eventListeners =
            new HashMap<>();

    private static EventBusCenter eventBusCenter;

    private final ThreadTaskExecutor threadPoolTaskExecutor;

    public EventBusCenter() {
        threadPoolTaskExecutor = ThreadPoolBuilder.build("event-bus", 10000);
    }

    public static EventBusCenter getInstance() {
        if (eventBusCenter == null) {
            eventBusCenter = new EventBusCenter();
        }
        return eventBusCenter;
    }

    public <T extends EventBody> void register(IEventListener<T> eventListener) {
        log.info("register eventBusCenter listener: {}", eventListener.name());
        List<IEventListener<? extends EventBody>> listeners = eventListeners.computeIfAbsent(eventListener.eventBodyClass(), k -> new ArrayList<>());
        listeners.add(eventListener);

    }

    public <T extends EventBody> void unRegister(IEventListener<T> eventListener) {
        List<IEventListener<? extends EventBody>> listeners = eventListeners.get(eventListener.eventBodyClass());
        if (listeners != null) {
            listeners.remove(eventListener);
        }
    }

    public void asyncPost(EventBody eventBody) {
        threadPoolTaskExecutor.execute(() -> {
            post(eventBody);
        });
    }

    public Object post(EventBody eventBody) {
        Object resp = null;
        if (eventBody == null) {
            return resp;
        }
        for (Map.Entry<Class<? extends EventBody>, List<IEventListener<? extends EventBody>>> entry : eventListeners.entrySet()) {
            if (eventBody.getClass().equals(entry.getKey())) {
                for (IEventListener listener : entry.getValue()) {
                    resp =listener.listen(eventBody);
                }
            }
        }
        return resp;
    }

    public static void async(EventBody event) {
        getInstance().asyncPost(event);
    }

    public static Object postSync(EventBody event){
        return getInstance().post(event);
    }

}
