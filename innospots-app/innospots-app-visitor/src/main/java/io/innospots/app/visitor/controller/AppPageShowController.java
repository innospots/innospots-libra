package io.innospots.app.visitor.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.model.response.InnospotsResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/26
 */
@RestController
@RequestMapping( PathConstant.APP_ROOT_PATH +"api")
@Tag(name = "Application Page Show")
public class AppPageShowController {

    @PostMapping("token/login")
    public InnospotsResponse<?> login(@RequestBody Map<String,Object> body){

        return null;
    }

    @GetMapping("access/check")
    public InnospotsResponse<?> valid(){

        return null;
    }

    @PostMapping("show-page/{appPath}")
    public InnospotsResponse<?> showPage(@PathVariable String appPath){

        return null;
    }

}
