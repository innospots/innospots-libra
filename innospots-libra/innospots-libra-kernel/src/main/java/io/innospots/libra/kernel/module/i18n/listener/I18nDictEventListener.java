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

package io.innospots.libra.kernel.module.i18n.listener;

import io.innospots.base.events.IEventListener;
import io.innospots.libra.kernel.events.I18nDictEvent;
import io.innospots.libra.kernel.module.i18n.model.I18nDictionary;
import io.innospots.libra.kernel.module.i18n.model.I18nTransMessageGroup;
import io.innospots.libra.kernel.module.i18n.operator.I18nDictionaryOperator;
import io.innospots.libra.kernel.module.i18n.operator.I18nTransMessageOperator;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * I18nDictEventListener
 *
 * @author Wren
 * @date 2022/2/5-22:32
 */
@Slf4j
@Component
@AllArgsConstructor
public class I18nDictEventListener implements IEventListener<I18nDictEvent> {

    private I18nDictionaryOperator i18nDictionaryOperator;

    private I18nTransMessageOperator i18nTransMessageOperator;


    @Override
    public Object listen(I18nDictEvent i18nDictEvent) {
        //TODO module info menu or opt
        I18nDictionary i18nDictionary = new I18nDictionary(i18nDictEvent.getExtensionName(), i18nDictEvent.getModule(), i18nDictEvent.getCode(), "");
        boolean result = i18nDictionaryOperator.saveOrUpdate(i18nDictionary);
        if (result) {
            if (i18nDictEvent.getI18nNames() != null) {
                i18nTransMessageOperator.updateTransMessageGroup(new I18nTransMessageGroup(i18nDictionary, i18nDictEvent.getI18nNames()));
            } else {
                log.warn("I18nName is null, {}", i18nDictEvent);
            }
        }
        return result;
    }
}
