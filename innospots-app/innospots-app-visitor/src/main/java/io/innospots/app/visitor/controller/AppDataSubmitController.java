package io.innospots.app.visitor.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/29
 */

@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api/data-submit")
@Tag(name = "Application Data Submit")
public class AppDataSubmitController {

    @RequestMapping("{appKey}/registry/{registryId}")
    public InnospotsResponse<?> submit(
            @PathVariable String appKey,
            @PathVariable String registryId,
            @RequestBody Map<String,Object> body){
        return null;
    }
}
