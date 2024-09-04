package io.innospots.app.visitor.controller;

import io.innospots.app.visitor.operator.AppDataOperator;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.model.response.InnospotsResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/29
 */
@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api/data")
@Tag(name = "Application Data View")
public class AppDataController {

    private final AppDataOperator appDataOperator;

    public AppDataController(AppDataOperator appDataOperator) {
        this.appDataOperator = appDataOperator;
    }

    @Operation(summary = "show and read data from table or api")
    @GetMapping("{appKey}/registry/{registryId}")
    public InnospotsResponse<Map<String,Object>> show(@PathVariable String appKey,
                                     @PathVariable String registryId,
                                     @RequestParam Map<String,Object> params){
        return InnospotsResponse.success(appDataOperator.show(appKey,registryId,params));
    }

    @Operation(summary = "show and read list data from table or api")
    @GetMapping("{appKey}/registry/{registryId}/list")
    public InnospotsResponse<List<Object>> list(@PathVariable String appKey,
                                                            @PathVariable String registryId,
                                                            @RequestParam Map<String,Object> params){

        return InnospotsResponse.success(appDataOperator.list(appKey,registryId,params));
    }

    @Operation(summary = "page list from table or api")
    @GetMapping("{appKey}/registry/{registryId}/page")
    public InnospotsResponse<PageBody<Map<String,Object>>> page(@PathVariable String appKey,
                                            @PathVariable String registryId,
                                            @RequestParam Map<String,Object> params){

        return InnospotsResponse.success(appDataOperator.page(appKey,registryId,params));
    }

    @Operation(summary = "write and submit data to table or api")
    @PostMapping("{appKey}/registry/{registryId}")
    public Object submit(
            @PathVariable String appKey,
            @PathVariable String registryId,
            @RequestBody Map<String,Object> body){
        return appDataOperator.submit(appKey,registryId,body);
    }

}
