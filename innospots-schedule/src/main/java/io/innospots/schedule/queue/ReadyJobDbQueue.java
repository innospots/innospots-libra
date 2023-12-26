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

package io.innospots.schedule.queue;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import io.innospots.base.utils.ServiceActionHolder;
import io.innospots.schedule.dao.ReadyJobDao;
import io.innospots.schedule.dao.ScheduleJobInfoDao;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.enums.MessageStatus;
import io.innospots.schedule.model.ReadyJob;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.RandomUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/4
 */
@Slf4j
public class ReadyJobDbQueue implements IReadyJobQueue {

    private ScheduleJobInfoDao scheduleJobInfoDao;

    private ReadyJobDao readyJobDao;

    public ReadyJobDbQueue(ScheduleJobInfoDao scheduleJobInfoDao, ReadyJobDao readyJobDao) {
        this.scheduleJobInfoDao = scheduleJobInfoDao;
        this.readyJobDao = readyJobDao;
    }

    /**
     * fetch ready execute jobs , msg status is undead , order by created time asc
     *
     * @param fetchSize fetch size
     * @return
     */
    @Override
    public List<ReadyJobEntity> poll(int fetchSize, List<String> groupKeys) {
        //select unread job
        List<ReadyJobEntity> readies = readyJobDao.selectUnReadJob(fetchSize, groupKeys);
        if (CollectionUtils.isEmpty(readies)) {
            return Collections.emptyList();
        }
        //update job status to assigned, and assign to current server
        for (ReadyJobEntity readyJobEntity : readies) {
            if (readies.size() > 1) {
                //Avoid overly concentrated preemption of assigned jobs
                try {
                    TimeUnit.MILLISECONDS.sleep(RandomUtils.nextInt(1, readies.size()));
                } catch (InterruptedException e) {
                    log.error(e.getMessage());
                }
            }
            readyJobEntity.setServerKey(ServiceActionHolder.getServerKey());
            //Update ready job with optimistic locks
            int count = readyJobDao.updateAssignedJob(readyJobEntity);
        }
        //update msg status to read and serverKey
        return readyJobDao.selectAssignJobs(ServiceActionHolder.getServerKey());
    }

    @Override
    public void ackRead(String jobReadyKey) {
        readyJobDao.updateStatusAssignedToRead(jobReadyKey);
    }

    @Override
    public void push(ReadyJob readyJob) {

    }

    @Override
    public void push(String jobKey) {

    }

    @Override
    public void resetAssignTimeoutJob() {

    }


}
