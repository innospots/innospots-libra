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

package io.innospots.base.enums;

import lombok.Getter;

/**
 * @author chenc
 * @date 2022/1/22
 */
@Getter
public enum ImageType {

    AVATAR("avatar",180,180,true),
    FAVICON("favicon",120,120,true),
    LOGO("logo",140,30,true),
    COMMENT("comment",500,500,false),
    NODE("node",120,120,true),
    CREDENTIAL("app",120,120,true),
    APP("app",120,120,true),
    OTHER("other",600,600,false);

    private String info;

    /** max width */
    private int width;
    /** max height */
    private int height;
    /** fix image scale*/
    private boolean fix;

    ImageType(String info,int width, int height,boolean fix) {
        this.info = info;
        this.height = height;
        this.width = width;
        this.fix = fix;
    }

    public static ImageType imageType(String info) {
        if (info == null) {
            return AVATAR;
        }
        return ImageType.valueOf(info.toUpperCase());
    }
}