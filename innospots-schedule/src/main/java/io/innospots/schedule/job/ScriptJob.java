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

import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.base.quartz.JobType;
import io.innospots.schedule.model.JobExecution;

import javax.script.*;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2023/12/9
 */
public class ScriptJob extends BaseJob {

    public static String PARAM_SCRIPT_TYPE = "job.script.type";
    public static String PARAM_SCRIPT_BODY = "job.script.body";

    public ScriptJob(JobExecution jobExecution) {
        super(jobExecution);
    }

    @Override
    public JobType jobType() {
        return JobType.EXECUTE;
    }

    @Override
    public InnospotsResponse<Map<String, Object>> execute() {
        InnospotsResponse<Map<String, Object>> response = new InnospotsResponse<>();
        String scriptType = validParamString(PARAM_SCRIPT_TYPE);
        String scriptBody = validParamString(PARAM_SCRIPT_BODY);

        Compilable compilable = compilable(scriptType);
        try {
            CompiledScript compiledScript = compilable.compile(scriptBody);
            Object val = compiledScript.eval(new SimpleBindings(jobExecution.getContext()));
            if (val != null) {
                String msg = val.toString();
                if (msg.length() > 32768) {
                    msg = msg.substring(0, 32768);
                }
                response.setMessage(msg);
            }
        } catch (ScriptException e) {
            throw new RuntimeException(e);
        }
        return response;
    }

    private Compilable compilable(String scriptType) {
        ScriptEngineManager engineManager = new ScriptEngineManager();
        return (Compilable) engineManager.getEngineByName(scriptType);
    }
}
