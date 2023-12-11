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

package io.innospots.schedule.operator;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper;
import io.innospots.base.events.EventBusCenter;
import io.innospots.base.quartz.ExecutionStatus;
import io.innospots.base.utils.InnospotsIdGenerator;
import io.innospots.schedule.converter.JobExecutionConverter;
import io.innospots.schedule.dao.JobExecutionDao;
import io.innospots.schedule.entity.JobExecutionEntity;
import io.innospots.schedule.entity.ReadyJobEntity;
import io.innospots.schedule.events.JobExecutionEvent;
import io.innospots.schedule.model.JobExecution;
import org.apache.commons.collections4.CollectionUtils;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/3
 */
public class JobExecutionOperator {

    private JobExecutionDao jobExecutionDao;

    private LocalDateTime lastUpdateTime;


    public JobExecution jobExecution(String jobExecutionId){
        return JobExecutionConverter.INSTANCE
                .entityToModel(jobExecutionDao.selectById(jobExecutionId));
    }

    /**
     * create job execution to database
     * @param readyJobEntity
     * @return
     */
    public JobExecution createJobExecution(ReadyJobEntity readyJobEntity) {
        //build job execution by ready Job entity
        JobExecutionEntity jobExecutionEntity = JobExecutionConverter.build(readyJobEntity);
        jobExecutionDao.insert(jobExecutionEntity);
        return JobExecutionConverter.INSTANCE.entityToModel(jobExecutionEntity);
    }

    /**
     * This class represents a job execution operator that can stop a job execution
     * with a given ID and message.
     */
    public void stop(String jobExecutionId,String message){
        jobExecutionDao.update(null,buildUpdateWrapper(jobExecutionId,ExecutionStatus.STOPPED,message));
    }

    public void complete(String jobExecutionId,String message){
        jobExecutionDao.update(null,buildUpdateWrapper(jobExecutionId,ExecutionStatus.COMPLETE,message));
    }



    public void fail(String jobExecutionId,String message){
        jobExecutionDao.update(null,buildUpdateWrapper(jobExecutionId,ExecutionStatus.FAILED,message));
    }

    public void updateJobExecution(JobExecution jobExecution){
        jobExecutionDao.updateById(JobExecutionConverter.INSTANCE.modelToEntity(jobExecution));
    }

    /**
     * job executions that have status is executing
     * @return
     */
    public List<JobExecution> fetchExecutingJobs(){
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getStatus,ExecutionStatus.executingStatus());
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * job executions that have status is done
     * @return
     */
    public List<JobExecution> fetchRecentDoneJobs(){
        if(lastUpdateTime == null){
            lastUpdateTime = LocalDateTime.now().minusMinutes(1);
        }
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().in(JobExecutionEntity::getStatus,ExecutionStatus.doneStatus())
                .ge(JobExecutionEntity::getUpdatedTime,lastUpdateTime);
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        return JobExecutionConverter.INSTANCE.entitiesToModels(entities);
    }

    /**
     * set job execution status to failed when timeout
     * @param jobExecution
     */
    public void updateTimeoutExecution(JobExecution jobExecution){
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getStatus, ExecutionStatus.FAILED)
               .set(JobExecutionEntity::getMessage,"job execute timeout")
               .eq(JobExecutionEntity::getExecutionId,jobExecution.getExecutionId());
        this.jobExecutionDao.update(null,uw);
    }

    /**
     * all sub job executions are completed
     * @param parentExecutionId
     * @return
     */
    public Set<String> completeJobKeys(String parentExecutionId){
        QueryWrapper<JobExecutionEntity> qw = new QueryWrapper<>();
        qw.lambda().eq(JobExecutionEntity::getParentExecutionId,parentExecutionId)
                .select(JobExecutionEntity::getKey);
        List<JobExecutionEntity> entities = jobExecutionDao.selectList(qw);
        if(CollectionUtils.isEmpty(entities)){
            return Collections.emptySet();
        }

        return entities.stream().map(JobExecutionEntity::getKey).collect(Collectors.toSet());

    }


    private UpdateWrapper<JobExecutionEntity> buildUpdateWrapper(String jobExecutionId,
                                                                 ExecutionStatus executionStatus,
                                                                 String message){
        UpdateWrapper<JobExecutionEntity> uw = new UpdateWrapper<>();
        uw.lambda().set(JobExecutionEntity::getStatus, executionStatus)
                .set(message!=null,JobExecutionEntity::getMessage,message)
                .set(JobExecutionEntity::getUpdatedTime,LocalDateTime.now())
                .eq(JobExecutionEntity::getExecutionId,jobExecutionId);

        return uw;
    }

}
