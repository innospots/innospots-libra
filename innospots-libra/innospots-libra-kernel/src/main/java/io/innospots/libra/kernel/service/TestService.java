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

package io.innospots.libra.kernel.service;

import io.innospots.base.utils.BeanContextAware;
import io.innospots.base.utils.BeanContextAwareUtils;
import io.innospots.libra.base.log.OperationLog;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * @author Smars
 * @date 2023/4/25
 */
@Component
@Slf4j
public class TestService {

    //@OperationLog(detail = "log1")
    public void log1(Map<String,Object> item){
      log.info("log1:{}",item);
      TestService ts = BeanContextAwareUtils.getBean(TestService.class);
      ts.log2(item);
    }

    @OperationLog(detail = "log2")
    public void log2(Map<String,Object> item){
        log.info("log2:{}",item);
        log3(item);
    }

    @OperationLog(detail = "log3")
    public void log3(Map<String,Object> item){
        log.info("log3:{}",item);
    }

}
