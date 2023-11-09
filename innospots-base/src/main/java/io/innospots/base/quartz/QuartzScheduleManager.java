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

package io.innospots.base.quartz;


import io.innospots.base.exception.InnospotException;
import io.innospots.base.model.response.ResponseCode;
import org.apache.commons.collections4.CollectionUtils;
import org.quartz.*;
import org.quartz.impl.DirectSchedulerFactory;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * quartz manager
 *
 * @author Raydian
 * @date 2020/12/15
 */
public class QuartzScheduleManager {

    private static final Logger logger = LoggerFactory.getLogger(QuartzScheduleManager.class);

    private Scheduler scheduler;

    private static final String JOB_DEFAULT_GROUP_NAME = "JOB_DEFAULT_GROUP_NAME";
    private static final String TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    private Integer quartzThreadNumber = 8;

    /**
     * update time
     */
    private LocalDateTime updateTime;


    /**
     * startup quartz
     */
    public void startup() {
        try {
            if (scheduler == null) {
                DirectSchedulerFactory.getInstance().createVolatileScheduler(quartzThreadNumber);
                scheduler = DirectSchedulerFactory.getInstance().getScheduler();
            }
            if (!scheduler.isStarted()) {
                logger.info("startup quartz manager");
                scheduler.start();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void validate() {
        try {
            if (scheduler == null || !scheduler.isStarted()) {
                throw InnospotException.buildException(this.getClass(), ResponseCode.EXECUTE_SCHEDULE_ERROR, "quartz is not started，please call startup");
            }
        } catch (SchedulerException e) {
            throw InnospotException.buildException(this.getClass(), ResponseCode.EXECUTE_SCHEDULE_ERROR, "quartz is not started，please call startup");
        }
    }


    public boolean refreshJob(String jobName, Class<? extends Job> jobClass, String cronExp, ScheduleMode frequency, Date startTime, Date endTime) {
        validate();
        boolean result = false;
        if (!ScheduleMode.ONCE.equals(frequency) && (!CronExpression.isValidExpression(cronExp))) {
            logger.error("Illegal cron expression format({})", cronExp);
            return result;
        }


        try {
            JobKey jobKey = new JobKey(jobName, JOB_DEFAULT_GROUP_NAME);
            TriggerKey triggerKey = TriggerKey.triggerKey(jobName, JOB_DEFAULT_GROUP_NAME);

            SimpleDateFormat formatter = new SimpleDateFormat(TIME_FORMAT);

            if (scheduler.checkExists(jobKey) && scheduler.checkExists(triggerKey)) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                if (ScheduleMode.ONCE.equals(frequency)) {

                    SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                            .forJob(jobDetail)
                            .withIdentity(triggerKey)
                            .startAt(startTime)
                            .build();

                    scheduler.rescheduleJob(triggerKey, trigger);
                } else if (cronExp != null) {
                    Trigger newTrigger = TriggerBuilder.newTrigger()
                            .forJob(jobDetail)
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExp))
                            .withIdentity(triggerKey)
//                            .startAt(startTime)
                            .endAt(endTime)
                            .build();
                    scheduler.rescheduleJob(triggerKey, newTrigger);
                } else {
                    logger.warn("cronExpression is null, jobName:{}", jobName);
                }
            } else {
                JobDetail jobDetail = JobBuilder.newJob().withIdentity(jobKey)
                        .ofType(jobClass)
                        .build();

                if (ScheduleMode.ONCE.equals(frequency)) {
                    SimpleTrigger trigger = (SimpleTrigger) TriggerBuilder.newTrigger()
                            .forJob(jobDetail)
                            .withIdentity(triggerKey)
                            .startAt(startTime)
                            .build();
                    scheduler.scheduleJob(jobDetail, trigger);
                } else if (cronExp != null) {
                    Trigger newTrigger = TriggerBuilder.newTrigger()
                            .forJob(jobDetail)
                            .withSchedule(CronScheduleBuilder.cronSchedule(cronExp))
                            .withIdentity(triggerKey)
//                            .startAt(startTime)
//                            .endAt(endTime)
                            .build();
                    scheduler.scheduleJob(jobDetail, newTrigger);

                } else {
                    logger.warn("cronExpression is null, jobName:{}", jobName);
                }
            }
            logger.info("add time scheduler, jobName:{}, cronExp:{}", jobName, cronExp);
            updateTime = LocalDateTime.now();
            result = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return result;
    }


    /**
     * delete job
     *
     * @param jobName
     * @return
     */
    public boolean deleteJob(String jobName) {
        validate();
        boolean result = false;


        JobKey jobKey = new JobKey(jobName, JOB_DEFAULT_GROUP_NAME);
        try {
            updateTime = LocalDateTime.now();
            if (scheduler.checkExists(jobKey)) {
                result = scheduler.deleteJob(jobKey);

            } else {
                logger.error("delete job name:{},group name:{} not exists.", jobKey.getName(), jobKey.getGroup());
            }
        } catch (SchedulerException e) {
            logger.error("delete job name:{},group name:{} failed!", jobKey.getName(), jobKey.getGroup(), e);
        }
        return result;
    }

    /**
     * pause job
     *
     * @param jobName
     */
    public void pauseJob(String jobName) {
        validate();


        JobKey jobKey = new JobKey(jobName, JOB_DEFAULT_GROUP_NAME);
        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.pauseJob(jobKey);
                updateTime = LocalDateTime.now();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * resume job
     *
     * @param jobName
     */
    public void resumeJob(String jobName) {
        validate();

        JobKey jobKey = new JobKey(jobName, JOB_DEFAULT_GROUP_NAME);

        try {
            if (scheduler.checkExists(jobKey)) {
                scheduler.resumeJob(jobKey);
                updateTime = LocalDateTime.now();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * close quartz
     */
    public void shutdown() {
        if (scheduler != null) {
            try {
                logger.info("close quartz scheduleManager.");
                scheduler.clear();
                scheduler.shutdown();
            } catch (SchedulerException e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    public boolean hasJob(String jobName) {
        try {
            return scheduler.checkExists(TriggerKey.triggerKey(String.valueOf(jobName), JOB_DEFAULT_GROUP_NAME));
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
            return false;
        }
    }

    public Set<String> scheduleJobs() {
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(JOB_DEFAULT_GROUP_NAME));

            if (CollectionUtils.isNotEmpty(jobKeys)) {
                return jobKeys.stream().map(JobKey::getName).collect(Collectors.toSet());
            } else {
                return Collections.emptySet();
            }
        } catch (SchedulerException e) {
            logger.error(e.getMessage(), e);
            return Collections.emptySet();
        }
    }

    public List<Map<String, Object>> schedulerInfo() {
        List<Map<String, Object>> schedulesList = new ArrayList<>();
        try {
            Set<JobKey> jobKeys = scheduler.getJobKeys(GroupMatcher.groupEquals(JOB_DEFAULT_GROUP_NAME));
            for (JobKey jobKey : jobKeys) {
                JobDetail jobDetail = scheduler.getJobDetail(jobKey);
                List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
                Map<String, Object> jobInfo = new LinkedHashMap<>();
                jobInfo.put("jobKeyName", jobKey.getName());
                jobInfo.put("jobKeyGroup", jobKey.getGroup());
                jobInfo.put("jobDetailDataMap", jobDetail.getJobDataMap());
                jobInfo.put("jobClass", jobDetail.getJobClass().getName());
                List<Map<String, Object>> triggerList = new ArrayList<>();
                for (Trigger trigger : triggers) {
                    Map<String, Object> triggerMap = new LinkedHashMap<>();
                    triggerMap.put("startTime", trigger.getStartTime());
                    triggerMap.put("nextFireTime", trigger.getNextFireTime());
                    triggerMap.put("endTime", trigger.getEndTime());
                    triggerMap.put("finalFireTime", trigger.getFinalFireTime());
                    triggerMap.put("calendarName", trigger.getCalendarName());
                    triggerMap.put("mayFireAgain", trigger.mayFireAgain());
                    triggerList.add(triggerMap);

                }
                jobInfo.put("triggers", triggerList);
                schedulesList.add(jobInfo);
            }//end for
        } catch (SchedulerException e) {
            logger.error(e.getMessage());
        }

        return schedulesList;
    }


    public LocalDateTime latestUpdateTime() {
        return updateTime;
    }

}
