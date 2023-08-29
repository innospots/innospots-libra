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

package io.innospots.base.function.aggregation;

/**
 * @author Smars
 * @version 1.2.0
 * @date 2023/2/28
 */
public enum AggregationFunctionType {


    /**
     *
     */
    SUM("summation","求和"),
    AVG("average","平均数"),
    COUNT("count","次数"),
    MAX("maximum","最大值"),
    MIN("minimum","最小值"),
    POP_VARIANCE("population variance","总体标准差"),
    POP_STD_DEV("population standard deviation","总体标准差"),
    STD_DEV("standard deviation","标准差"),
    VARIANCE("variance","方差"),//
    MEDIAN("median","中位数"),
    MODE("mode","众数"),
    DISCOUNT("distinct","非重复计数"),
    SEM("standard error of mean","均值标准误差"),
    MAD("mean absolute deviation","平均绝对差"),
    PROD("product","连乘"),
    SKEW("sample skewness","样本偏度（第三阶）"),
    KURT("kurtosis","样本峰度（第四阶）"),
    SOS("sum of squares","平方和"),
    GeometricMean("geometric mean","几何平均数"),
    SumOfLogs("sum of log","log求和"),
    PERCENTILE("percentile","百分位数"),
    SEMI_VAR("semi variance","半方差")
    ;


    private String desc;

    private String descCn;

    private Class<IAggregationFunction> functionClass;

    AggregationFunctionType(String desc, String descCn) {
        this.desc = desc;
        this.descCn = descCn;
    }

    public Class<IAggregationFunction> functionClass(){
        return functionClass;
    }
}
