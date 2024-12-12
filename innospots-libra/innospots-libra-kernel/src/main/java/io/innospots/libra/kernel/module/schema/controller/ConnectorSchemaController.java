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

package io.innospots.libra.kernel.module.schema.controller;

import io.innospots.connector.core.meta.ConnectionMinderSchema;
import io.innospots.connector.core.meta.ConnectionMinderSchemaLoader;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

import static io.innospots.base.model.response.R.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/3/8
 */

@RestController
@RequestMapping(PATH_ROOT_ADMIN + "schema/connector")
@ModuleMenu(menuKey = "libra-dataset")
@Tag(name = "App Schema Connector Config")
public class ConnectorSchemaController {

    public ConnectorSchemaController() {
    }

    @GetMapping("config/list")
    @Operation(description = "Query Config list")
    public R<List<ConnectionMinderSchema>> configList() {
        List<ConnectionMinderSchema> schemaList = ConnectionMinderSchemaLoader.connectionMinderSchemas();
        return success(schemaList);
    }

}
