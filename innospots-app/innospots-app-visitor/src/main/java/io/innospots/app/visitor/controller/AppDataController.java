package io.innospots.app.visitor.controller;

import io.innospots.app.visitor.operator.AppDataOperator;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.response.R;
import io.innospots.libra.base.utils.ImageFileUploader;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/29
 */
@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api/data")
@Tag(name = "Application Data Operator")
public class AppDataController {

    private final AppDataOperator appDataOperator;

    public AppDataController(AppDataOperator appDataOperator) {
        this.appDataOperator = appDataOperator;
    }

    @Operation(summary = "show and read data from table or api")
    @GetMapping("{appKey}/registry/{registryId}")
    public R<Map<String,Object>> show(@PathVariable String appKey,
                                      @PathVariable String registryId,
                                      @RequestParam Map<String,Object> params){
        return R.success(appDataOperator.show(appKey,registryId,params));
    }

    @Operation(summary = "show and read list data from table or api")
    @GetMapping("{appKey}/registry/{registryId}/list")
    public R<List<Object>> list(@PathVariable String appKey,
                                @PathVariable String registryId,
                                @RequestParam Map<String,Object> params){

        return R.success(appDataOperator.list(appKey,registryId,params));
    }

    @Operation(summary = "page list from table or api")
    @GetMapping("{appKey}/registry/{registryId}/page")
    public R<PageBody<Map<String,Object>>> page(@PathVariable String appKey,
                                                @PathVariable String registryId,
                                                @RequestParam Map<String,Object> params){

        return R.success(appDataOperator.page(appKey,registryId,params));
    }

    @Operation(summary = "write and submit data to table or api")
    @PostMapping("{appKey}/registry/{registryId}")
    public Object submit(
            @PathVariable String appKey,
            @PathVariable String registryId,
            @RequestBody Map<String,Object> body){
        return appDataOperator.submit(appKey,registryId,body);
    }


    @Operation(summary = "upload file")
    @PostMapping("{appKey}/upload")
    public R<ExecutionResource> upload(
            @Parameter(name = "appKey",required = true) @PathVariable("appKey") String appKey,
            @Parameter(name = "upFile", required = true) @RequestParam("upFile") MultipartFile upFile){
        ExecutionResource executionResource = ImageFileUploader.upload(upFile,"apps/api/data/resource/"+appKey);
        return R.success(executionResource);
    }

    @Operation(summary = "download resource")
    @GetMapping("resource/{appKey}")
    public ResponseEntity resource(
            @Parameter(name = "appKey",required = true) @PathVariable("appKey") String appKey,
            @Parameter(name = "resourceId", required = true) @RequestParam("resourceId") String resourceId){
        ExecutionResource executionResource = ExecutionResource.buildResource(resourceId);
        MediaType mediaType = null;
        if(executionResource.getMimeType()!=null){
            String[] ss = executionResource.getMimeType().split("/");
            mediaType = new MediaType(ss[0], ss[1]);
        }else{
            mediaType = MediaType.valueOf(MediaType.APPLICATION_OCTET_STREAM_VALUE);
        }
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename*=UTF-8''" + URLEncoder.encode(executionResource.getResourceName(), StandardCharsets.UTF_8));
        return ResponseEntity
                .ok()
                .headers(headers)
                .contentType(mediaType)
                .body(executionResource.buildInputStreamSource());
    }

}
