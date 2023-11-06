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

package io.innospots.administration.schedule;

import io.innospots.base.events.EventBusCenter;
import io.innospots.libra.base.configuration.InnospotsConsoleProperties;
import io.innospots.libra.kernel.events.LogClearEvent;
import io.innospots.server.base.registry.ServiceRegistryHolder;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * DataCleanService
 *
 * @author Wren
 * @date 2022/11/17-21:36
 */
@Slf4j
@Service
@AllArgsConstructor
public class LoggerClearScheduler {


    private InnospotsConsoleProperties inspectionProperties;

    //sys_operation_log，同样可设置清理历史时长和最大保留记录。默认设置为不清理

    @Scheduled(cron = "0 10 4 * * ?")
    private void SysOperateLogCleanTask() {
        try {
            if (ServiceRegistryHolder.isLeader() && inspectionProperties.isEnableCleanSysOperateLog()) {
                int days = inspectionProperties.getSysOperateLogKeepDays();
                int count = inspectionProperties.getSysLoginLogKeepAmount();
                LogClearEvent logClearEvent = new LogClearEvent(count, days);
                Object delCount = EventBusCenter.postSync(logClearEvent);
                log.info("SysOperateLogCleanTask delete:{}, days:{} currTime:{}", delCount, days, LocalDateTime.now());
            } else {
                log.info("SysOperateLogCleanTask not run!  curr service leader:{} enableCleanSysOperateLog:{} {}", ServiceRegistryHolder.isLeader(), inspectionProperties.isEnableCleanSysOperateLog(), ServiceRegistryHolder.getCurrentServer());
            }
        } catch (Exception e) {
            log.error("SysOperateLogCleanTask error:{}", e.getMessage(), e);
        }

    }
}
