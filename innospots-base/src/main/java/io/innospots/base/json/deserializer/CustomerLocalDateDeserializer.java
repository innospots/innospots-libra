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

package io.innospots.base.json.deserializer;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import io.innospots.base.i18n.LocaleContext;
import io.innospots.base.i18n.LocaleMessageUtils;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 * @author Smars
 * @version 1.0.0
 * @date 2021/12/21
 */
public class CustomerLocalDateDeserializer extends StdDeserializer<LocalDate> {


    protected CustomerLocalDateDeserializer(Class<?> vc) {
        super(vc);
    }

    public static CustomerLocalDateDeserializer getInstance() {
        return new CustomerLocalDateDeserializer(LocalDate.class);
    }

    @Override
    public LocalDate deserialize(JsonParser parser, DeserializationContext context) throws IOException {

        LocaleContext localeContext = LocaleMessageUtils.getLocaleContext();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern(localeContext.getDateFormat())
                .withZone(localeContext.getTimeZone().toZoneId())
                .withLocale(localeContext.getLocale());

        return LocalDate.parse(parser.getText(), formatter);
    }
}
