/*
 *  Copyright © 2021-2023 Innospots (http://www.innospots.com)
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License. You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package io.innospots.libra.kernel.module.system.controller;

import io.innospots.base.enums.ImageType;
import io.innospots.base.model.response.InnospotsResponse;
import io.innospots.libra.kernel.module.system.entity.AvatarResourceEntity;
import io.innospots.base.enums.ImageResource;
import io.innospots.libra.kernel.module.system.operator.AvatarResourceOperator;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.InputStreamSource;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Base64;

import static io.innospots.base.model.response.InnospotsResponse.success;
import static io.innospots.libra.base.controller.BaseController.PATH_ROOT_ADMIN;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/12
 */
@Controller
@RequestMapping(PATH_ROOT_ADMIN + "image")
public class AvatarImageController {

    private final AvatarResourceOperator avatarResourceOperator;

    public AvatarImageController(AvatarResourceOperator avatarResourceOperator) {
        this.avatarResourceOperator = avatarResourceOperator;
    }

    @PostMapping(value = "{imageType}")
    @ResponseBody
    public InnospotsResponse<String> upload(@PathVariable("imageType") ImageType imageType,
                                            @Parameter(name = "image", required = true) @RequestParam("image") MultipartFile uploadFile) {

        return success(avatarResourceOperator.upload(imageType, uploadFile));
    }

    @GetMapping(value = "{imageType}/{resourceId}")
    @ResponseBody
    public ResponseEntity show(@PathVariable("imageType") String imageType,
                               @PathVariable("resourceId") String resourceId,
                               @RequestParam(value = "imageSort", required = false) Integer imageSort,
                               HttpServletResponse servletResponse) throws IOException {
        //TODO add user images
        ImageType imageEnum = ImageType.imageType(imageType);
        AvatarResourceEntity avatarResource = avatarResourceOperator.getByResourceIdAndTypeAndSort(resourceId, imageEnum, imageSort);
        if (avatarResource != null && avatarResource.getImageBase64() != null) {
            ImageResource imageResource = ImageResource.imageResource(avatarResource.getImageBase64());
            if (imageResource != null) {
                if (imageResource.isLink()) {
                    servletResponse.sendRedirect(avatarResource.getImageBase64());
                } else {
                    InputStreamSource resource;
                    if (imageResource == ImageResource.SVG_XML) {
                        resource = new ByteArrayResource(avatarResource.getImageBase64().getBytes());
                    } else {

                        resource = new ByteArrayResource(Base64.getDecoder().decode(avatarResource.getImageBase64().replaceAll("data:image/\\w+;base64,", "")));
                    }
                    String[] ss = imageResource.getContentType().split("/");
                    return ResponseEntity.ok().contentType(new MediaType(ss[0], ss[1])).body(resource);
                }
            }
        }
        return null;
    }
}
