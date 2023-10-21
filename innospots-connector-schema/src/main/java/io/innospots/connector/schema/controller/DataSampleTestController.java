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

package io.innospots.connector.schema.controller;

import io.innospots.base.data.ap.IDataSenderPoint;
import io.innospots.base.data.minder.DataConnectionMinderManager;
import io.innospots.base.data.schema.ConnectionCredential;
import io.innospots.base.data.schema.reader.IConnectionCredentialReader;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.libra.base.controller.BaseController;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static io.innospots.base.model.response.InnospotResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Alfred
 * @date 2022/9/8
 */
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "apps/schema/sample/test")
@Tag(name = "Schema Data Sample Test")
public class DataSampleTestController extends BaseController {

    private final IConnectionCredentialReader connectionCredentialReader;

    private final IDataSenderPoint dataSenderPoint;

    private DataConnectionMinderManager connectionMinderManager;

    public DataSampleTestController(IConnectionCredentialReader connectionCredentialReader,
                                    DataConnectionMinderManager connectionMinderManager,
                                    IDataSenderPoint dataSenderPoint) {
        this.connectionCredentialReader = connectionCredentialReader;
        this.dataSenderPoint = dataSenderPoint;
        this.connectionMinderManager = connectionMinderManager;
    }

    @PostMapping("send/message/{code}")
    @Operation(summary = "send message to kafka datasource")
    public InnospotResponse<Boolean> sendMessage(
            @Parameter(name = "code") @PathVariable String code,
            @Parameter(name = "topic") @RequestParam(name = "topic") String topic,
            @Parameter(name = "data") @RequestBody Map<String, Object> data) {
        ConnectionCredential connectionCredential = connectionCredentialReader.readCredential(code);
        dataSenderPoint.send(connectionCredential.getCredentialId(), topic, data);
        return success(true);
    }

//    @PostMapping("receive/message/{datasourceId}")
//    @ResourceItemOperation
//    @Operation(summary = "receive message to kafka")
//    public InnospotResponse<DataBody<Map<String, Object>>> receive(
//            @PathVariable Integer datasourceId,
//            @RequestParam String topic,
//            @RequestParam(required = false, defaultValue = "test_sample") String group,
//            @RequestParam(required = false, defaultValue = "latest") String offset
//    ) {
//        IDataConnectionMinder minder = connectionMinderManager.getMinder(datasourceId);
//        if(minder==null){
//            throw ResourceException.buildNotExistException(this.getClass(),datasourceId,topic,group,offset);
//        }
//        IDataReceiver dataReceiver = minder.dataReceiver(topic, group, offset, MessageFormat.JSON.name(),3000,1);
//        return InnospotResponse.success(dataReceiver.receive());
//    }

}
