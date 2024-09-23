package io.innospots.libra.kernel.controller;

import io.innospots.base.constant.PathConstant;
import io.innospots.base.crypto.EncryptorBuilder;
import io.innospots.base.execution.ExecutionResource;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.IOException;

import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/22
 */
@Slf4j
@Tag(name = "System Resource")
@RestController
@RequestMapping(PATH_ROOT_ADMIN + "system")
public class ResourceController {

    @GetMapping(value = "resources")
    @ResponseBody
    public ResponseEntity resource(@RequestParam("resourceId") String resourceId) throws IOException {
        String uri = EncryptorBuilder.encryptor.decode(resourceId);
        File resFile = new File(uri);
        log.debug("resource file:{}",uri);
        ExecutionResource executionResource = ExecutionResource.buildResource(resFile,true, PathConstant.RESOURCE_PATH);
        InputStreamSource resource = executionResource.buildInputStreamSource();
        String[] ss = executionResource.getMimeType().split("/");
        return ResponseEntity.ok().contentType(new MediaType(ss[0], ss[1])).body(resource);
    }
}
