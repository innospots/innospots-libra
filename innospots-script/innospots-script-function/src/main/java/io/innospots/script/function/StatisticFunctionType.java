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

package io.innospots.script.function;

import io.innospots.script.function.aggregation.*;
import io.innospots.script.function.moving.*;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/28
 */
public enum StatisticFunctionType {


    /**
     *
     */
    SUM("summation", "求和", SumFunction.class, MovingSumFunction.class),
    AVG("average", "平均数", AverageFunction.class, MovingAvgFunction.class),
    COUNT("count", "次数", CountFunction.class, MovingCountFunction.class),
    MAX("maximum", "最大值", MaxFunction.class, MovingMaxFunction.class),
    MIN("minimum", "最小值", MinFunction.class,MovingMinFunction.class),
    STD_DEV("standard deviation", "标准差", StdDeviationFunction.class,MovingStdDeviationFunction.class),
    VARIANCE("variance", "方差", VarianceFunction.class,MovingVarianceFunction.class),//
    MEDIAN("median", "中位数", MedianFunction.class,MovingMedianFunction.class),
    MODE("mode", "众数", ModeFunction.class,MovingModeFunction.class),
    DISCOUNT("distinct", "非重复计数", DistinctFunction.class,MovingDistinctFunction.class),
    SEM("standard error of mean", "均值标准误差", StdErrMeanFunction.class,MovingStdErrMeanFunction.class),
    MAD("mean absolute deviation", "平均绝对差", MeanAbsDeviationFunction.class,MovingMeanAbsDeviationFunction.class),
    PROD("product", "连乘", ProductFunction.class,MovingProductFunction.class),
    SKEW("sample skewness", "样本偏度（第三阶）", SkewnessFunction.class,MovingSkewnessFunction.class),
    KURT("kurtosis", "样本峰度（第四阶）", KurtosisFunction.class,MovingKurtosisFunction.class),
    SOS("sum of squares", "平方和", SumOfSquareFunction.class,MovingSumOfSquareFunction.class),
    POP_VARIANCE("population variance", "总体方差", PopVarianceFunction.class,MovingPopVarianceFunction.class),
    POP_STD_DEV("population standard deviation", "总体标准差", PopStdDeviationFunction.class, MovingPopStdDeviationFunction.class),
    GEOMETRIC_MEAN("geometric mean", "几何平均数", GeometricMeanFunction.class,MovingPopStdDeviationFunction.class),
    SUM_OF_LOGS("sum of log", "log求和", SumOfLogsFunction.class,MovingSumOfLogsFunction.class),
    PERCENTILE("percentile", "百分位数", PercentileFunction.class,MovingPercentileFunction.class),
    SEMI_VAR("semi variance", "半方差", SemiVarianceFunction.class,MovingSemiVarianceFunction.class);

    private String desc;

    private String descCn;

    private Class<? extends IAggregationFunction> aggFuncClass;

    private Class<? extends IMovingFunction> movingFunctionClass;

    StatisticFunctionType(String desc, String descCn,
                          Class<? extends IAggregationFunction> aggFuncClass,
                          Class<? extends IMovingFunction> movingFunctionClass
    ) {
        this.desc = desc;
        this.descCn = descCn;
        this.aggFuncClass = aggFuncClass;
        this.movingFunctionClass = movingFunctionClass;
    }

    public Class<? extends IAggregationFunction> aggFuncClass() {
        return aggFuncClass;
    }

    public Class<? extends IMovingFunction> movingFuncClass() {
        return movingFunctionClass;
    }

    public String getDesc() {
        return desc;
    }

    public String getDescCn() {
        return descCn;
    }
}
