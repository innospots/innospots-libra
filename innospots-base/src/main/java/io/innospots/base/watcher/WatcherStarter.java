/*
 * Copyright © 2021-2023 Innospots (http://www.innospots.com)
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package io.innospots.base.watcher;

import io.innospots.base.utils.BeanContextAwareUtils;
import org.apache.commons.collections4.MapUtils;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.annotation.Order;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/6
 */
@Order
public class WatcherStarter implements ApplicationRunner {

    private WatcherSupervisor watcherSupervisor;

    public WatcherStarter(WatcherSupervisor watcherSupervisor) {
        this.watcherSupervisor = watcherSupervisor;
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String,IWatcher> watcherMap = BeanContextAwareUtils.getBeansOfType(IWatcher.class);
        if(MapUtils.isNotEmpty(watcherMap)){
            watcherMap.values().forEach(watcherSupervisor::registry);
        }
    }
}
