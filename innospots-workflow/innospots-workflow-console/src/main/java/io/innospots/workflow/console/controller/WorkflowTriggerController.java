package io.innospots.workflow.console.controller;


import cn.hutool.core.util.RandomUtil;
import io.innospots.connector.core.schema.model.SchemaField;
import io.innospots.connector.core.schema.model.SchemaRegistry;
import io.innospots.connector.core.schema.model.SchemaRegistryType;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.menu.ModuleMenu;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

@RestController
@RequestMapping(PATH_ROOT_ADMIN + "workflow/trigger")
@ModuleMenu(menuKey = "workflow-management")
@Tag(name = "Workflow Trigger")
public class WorkflowTriggerController {


    @Operation(summary = "page webhook registries")
    @GetMapping("webhook/registries")
    public R<PageBody<SchemaRegistry>> pageWebhookTriggerRegistry(
            @Parameter(name = "flowName") @RequestParam(required = false) String flowName,
            @Parameter(name = "categoryId") @RequestParam(required = false) Integer categoryId,
            @Parameter(name = "page") @RequestParam(required = false,defaultValue = "1") Integer page,
            @Parameter(name = "size") @RequestParam(required = false,defaultValue = "20") Integer size
    ){
        List<SchemaRegistry> registries = new ArrayList<>();
        for (int i = 1; i < 6; i++) {
            SchemaRegistry registry = new SchemaRegistry();
            registry.setRegistryId(RandomUtil.randomNumbers(6));
            registry.setCode("flowCode_"+i);
            registry.setRegistryType(SchemaRegistryType.WORKFLOW);
            registry.setName("flowName_"+i);
            registry.addConfig("requestType","POST");
            List<SchemaField> fields = new ArrayList<>();
            for (int j = 0; j < 5; j++) {
                SchemaField sf = new SchemaField();
                sf.setCode("code"+j);
                sf.setName("name"+j);
                sf.setFieldScope(FieldScope.BODY);
                sf.setComment("comment");
                sf.setValueType(FieldValueType.STRING);
                fields.add(sf);
            }
            registry.setSchemaFields(fields);

            registries.add(registry);
        }//end for
        PageBody pageBody = new PageBody<>();
        pageBody.setList(registries);
        pageBody.setCurrent(1L);
        pageBody.setTotal((long) registries.size());
        pageBody.setPageSize(20L);
        return R.success(pageBody);
    }
}
