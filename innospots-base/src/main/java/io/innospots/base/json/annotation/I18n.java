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

package io.innospots.base.json.annotation;

import com.fasterxml.jackson.annotation.JacksonAnnotationsInside;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import io.innospots.base.i18n.I18nUtils;
import io.innospots.base.i18n.LocaleMessageUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.annotation.*;
import java.util.Objects;


/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/19
 */
@Retention(RetentionPolicy.RUNTIME)
@Documented
@JacksonAnnotationsInside
@Target(ElementType.FIELD)
@JsonSerialize(using = I18n.I18nFieldSerializer.class)
public @interface I18n {

    class I18nFieldSerializer extends JsonSerializer<String> {

        @Override
        public void serialize(String o, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException {
            if (!StringUtils.isEmpty(o)) {
                if (I18nUtils.isI18nField(o)) {
                    String key = o.substring(2, o.length() - 1);
                    String defValue = null;
                    int p = key.indexOf(":");
                    if (p > 0) {
                        defValue = key.substring(p + 1);
                        key = key.substring(0, p);
                    }
                    String v = LocaleMessageUtils.message(key);
                    if (StringUtils.isEmpty(v)) {
                        v = defValue != null ? defValue : key;
                    }else if(Objects.equals(v,key) && defValue != null){
                        v = defValue;
                    }

                    if (StringUtils.isNotEmpty(v)) {
                        jsonGenerator.writeString(v);
                    } else {
                        jsonGenerator.writeString(key);
                    }
                } else {//end startWith if
                    jsonGenerator.writeString(o);
                }
            } else {
                jsonGenerator.writeString("");
            }
        }
    }

}
