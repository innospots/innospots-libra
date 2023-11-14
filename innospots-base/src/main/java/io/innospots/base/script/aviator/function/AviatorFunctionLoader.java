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

package io.innospots.base.script.aviator.function;

import com.googlecode.aviator.AviatorEvaluator;
import io.innospots.base.script.function.Regular;
import io.innospots.base.script.function.aggregation.UdafAvg;
import io.innospots.base.script.function.aggregation.UdafCount;
import io.innospots.base.script.function.aggregation.UdafSum;
import io.innospots.base.script.function.date.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Smars
 * @date 2021/9/5
 */
public class AviatorFunctionLoader {

    private static final Logger logger = LoggerFactory.getLogger(AviatorFunctionLoader.class);

    private static boolean loaded;

    public static void load() {
        if (loaded) {
            return;
        }
        try {
            AviatorEvaluator.addStaticFunctions("time", Dcal.class);
            AviatorEvaluator.addStaticFunctions("time", Dtcal.class);
            AviatorEvaluator.addStaticFunctions("time", Tcal.class);
            AviatorEvaluator.addStaticFunctions("ts", Tscal.class);
            AviatorEvaluator.addStaticFunctions("time", DtDiff.class);
            AviatorEvaluator.addStaticFunctions("time", TF.class);
            AviatorEvaluator.addStaticFunctions("ts", TsDiff.class);
            AviatorEvaluator.addStaticFunctions("ts", Ts.class);
            AviatorEvaluator.addStaticFunctions("ts", TsEx.class);
            AviatorEvaluator.addStaticFunctions("regular", Regular.class);
            AviatorEvaluator.addStaticFunctions("af", UdafCount.class);
            AviatorEvaluator.addStaticFunctions("af", UdafSum.class);
            AviatorEvaluator.addStaticFunctions("af", UdafAvg.class);
        } catch (IllegalAccessException | NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        }
        loaded = true;
    }

}
