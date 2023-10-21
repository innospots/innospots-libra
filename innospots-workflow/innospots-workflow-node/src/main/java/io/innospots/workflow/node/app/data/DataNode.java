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

package io.innospots.workflow.node.app.data;

import com.google.common.base.Enums;
import io.innospots.base.condition.Factor;
import io.innospots.base.data.ap.IDataOperatorPoint;
import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.data.operator.UpdateItem;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.DataBody;
import io.innospots.base.model.PageBody;
import io.innospots.base.model.response.InnospotResponse;
import io.innospots.base.utils.ApplicationContextUtils;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.enums.OutputFieldMode;
import io.innospots.workflow.core.enums.OutputFieldType;
import io.innospots.workflow.core.execution.ExecutionInput;
import io.innospots.workflow.core.execution.node.NodeExecution;
import io.innospots.workflow.core.execution.node.NodeOutput;
import io.innospots.workflow.core.node.app.BaseAppNode;
import io.innospots.workflow.core.node.instance.NodeInstance;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2021/3/16
 */
@Slf4j
public class DataNode extends BaseDataNode {

    private static final Logger logger = LoggerFactory.getLogger(DataNode.class);

    protected IDataOperatorPoint dataOperatorPoint;

    public static final String FIELD_CREDENTIAL_ID = "credential";

    protected Integer credentialId;


    @Override
    protected void initialize(NodeInstance nodeInstance) {
        super.initialize(nodeInstance);
        credentialId = nodeInstance.valueInteger(FIELD_CREDENTIAL_ID);
        dataOperatorPoint = ApplicationContextUtils.getBean(IDataOperatorPoint.class);
    }

}
