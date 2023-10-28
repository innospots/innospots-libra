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

package io.innospots.base.json.serializer;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import io.innospots.base.i18n.LocaleContext;
import io.innospots.base.i18n.LocaleMessageUtils;
import org.apache.commons.lang3.time.FastDateFormat;

import java.io.IOException;
import java.sql.Date;


/**
 * @author Alfred
 * @date 2022/12/4
 */
public class CustomerSqlDateSerializer extends StdSerializer<Date> {


    public CustomerSqlDateSerializer(Class<Date> t) {
        super(t);
    }

    public static CustomerSqlDateSerializer getInstance() {
        return new CustomerSqlDateSerializer(Date.class);
    }

    @Override
    public void serialize(Date value, JsonGenerator g, SerializerProvider provider) throws IOException {
        LocaleContext localeContext = LocaleMessageUtils.getLocaleContext();
        FastDateFormat sdf = FastDateFormat
                .getInstance(
                        localeContext.getDateFormat(),
                        localeContext.getTimeZone());

        g.writeString(sdf.format(value));
    }
}
