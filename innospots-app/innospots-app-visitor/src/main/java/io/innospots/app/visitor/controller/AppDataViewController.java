package io.innospots.app.visitor.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/29
 */
@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api/data")
@Tag(name = "Application Data View")
public class AppDataViewController {

    @GetMapping("view/{appKey}/")
    public InnospotsResponse<?> show(@PathVariable String appKey,
                                     @RequestParam Map<String,Object> params){

        return null;
    }

}
