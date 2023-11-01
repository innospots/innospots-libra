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

package io.innospots.libra.kernel.events;

import io.innospots.base.data.enums.DataOperation;
import io.innospots.base.events.EventBody;
import lombok.Getter;
import lombok.Setter;
import org.springframework.context.ApplicationEvent;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/3
 */
@Getter
@Setter
public class CredentialEvent extends EventBody {

    private DataOperation operation;

    private Integer credentialId;

    public CredentialEvent(Object source) {
        super(source);
    }

    public CredentialEvent(DataOperation operation, Integer credentialId) {
        super(credentialId);
        this.operation = operation;
        this.credentialId = credentialId;
    }
}
