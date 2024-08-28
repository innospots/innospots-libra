package io.innospots.app.visitor.controller;

import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.visitor.model.AppToken;
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


    @PostMapping("{appPath}/access/check")
    public InnospotsResponse<AppToken> valid(@PathVariable String appPath,
                                             Map<String,Object> body){

        return null;
    }

    @PostMapping("show-page/{appPath}")
    public InnospotsResponse<AppDefinition> showPage(@PathVariable String appPath){

        return null;
    }

}
