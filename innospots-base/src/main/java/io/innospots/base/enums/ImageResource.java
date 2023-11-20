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

package io.innospots.base.enums;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2022/8/12
 */
public enum ImageResource {

    /**
     *
     */
    PNG("image/png", "data:image/png"),
    SVG("image/svg+xml", "data:image/svg"),
    SVG_XML("image/svg+xml", "<svg"),
    JPEG("image/jpeg", "data:image/jpeg"),
    HTTP("http", "http"),
    URI("uri", "/");

    private String contentType;

    private String prefix;

    ImageResource(String contentType, String prefix) {
        this.contentType = contentType;
        this.prefix = prefix;
    }


    public static ImageResource imageResource(String resource) {
        for (ImageResource value : ImageResource.values()) {
            if (resource.startsWith(value.prefix)) {
                return value;
            }
        }

        return ImageResource.URI;
    }

    public boolean isLink() {
        return this == HTTP || this == URI;
    }

    public String getContentType() {
        return contentType;
    }
}
