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

package io.innospots.schedule.job;

import cn.hutool.core.util.RuntimeUtil;
import io.innospots.base.model.response.ResponseCode;
import io.innospots.schedule.enums.JobType;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * shell cmd job
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
@Slf4j
public class CommandJob extends BaseJob{

    public static String PARAM_COMMAND = "job.cmd";
    public static String PARAM_FILE = "job.file";
    public static String PARAM_ARGS = "job.args";


    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public void execute() {
        String cmd = validParamString(PARAM_COMMAND);
        String cmdFile = jobExecution.getString(PARAM_FILE);
        String[] args = jobExecution.getStringArray(PARAM_ARGS);
        List<String> cmdList = new ArrayList<>();
        cmdList.add(cmd);
        if(cmdFile!=null){
            cmdList.add(cmdFile);
        }
        if(args!=null){
            Collections.addAll(cmdList, args);
        }
        String resp = RuntimeUtil.execForStr(cmdList.toArray(new String[]{}));
        log.info("execute cmd job:{}, cmd:{}, response:{}",this.jobExecution.getExecutionId(),cmdList,resp);
        if(resp.length() > 32768){
            resp = resp.substring(0, 32768);
        }
        jobExecution.setMessage(resp);
    }
}
