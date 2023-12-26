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

import io.innospots.base.model.response.ResponseCode;
import io.innospots.base.script.ExecutorManagerFactory;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.schedule.exception.JobExecutionException;
import io.innospots.schedule.model.JobExecution;

import javax.script.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
public class ScriptJob extends BaseJob {

    public static String PARAM_SCRIPT_TYPE = "job.script.type";
    public static String PARAM_SCRIPT_BODY = "job.script.body";

    @Override
    public void execute(JobExecution jobExecution) {
        String scriptType = jobExecution.getString(PARAM_SCRIPT_TYPE);
        String scriptBody = jobExecution.getString(PARAM_SCRIPT_BODY);
        if(scriptBody==null){
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, "script body is null");
        }
        if(scriptType==null){
            throw new JobExecutionException(this.getClass(), ResponseCode.PARAM_NULL, "script type is null");
        }
        Compilable compilable = compilable(scriptType);
        try {
            CompiledScript compiledScript = compilable.compile(scriptBody);
            Object val = compiledScript.eval(new SimpleBindings(jobExecution.getContext()));
            if(val!=null){
                String msg = val.toString();
                if(msg.length()>32768){
                    msg = msg.substring(0,32768);
                }
                jobExecution.setMessage(msg);
            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
    }

    private Compilable compilable(String scriptType) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        return (Compilable) engineManager.getEngineByName(scriptType);
    }
}
