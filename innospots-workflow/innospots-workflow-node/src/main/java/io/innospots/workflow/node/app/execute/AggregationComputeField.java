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

package io.innospots.workflow.node.app.execute;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.condition.EmbedCondition;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.BaseField;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.re.IExpression;
import io.innospots.base.re.aviator.AviatorExpression;
import io.innospots.base.function.aggregation.AggregationFunctionType;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.node.field.NodeParamField;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.math3.stat.descriptive.AbstractStorelessUnivariateStatistic;
import org.apache.commons.math3.stat.descriptive.moment.*;
import org.apache.commons.math3.stat.descriptive.rank.Median;
import org.apache.commons.math3.stat.descriptive.summary.Product;
import org.apache.commons.math3.stat.descriptive.summary.Sum;
import org.apache.commons.math3.stat.descriptive.summary.SumOfLogs;
import org.apache.commons.math3.stat.descriptive.summary.SumOfSquares;

import java.math.BigDecimal;
import java.util.*;

/**
 * aggregation compute field
 *
 * @author Smars
 * @date 2021/8/22
 */
@Getter
@Setter
@Slf4j
public class AggregationComputeField extends BaseField implements Initializer {


    private NodeParamField summaryField;

    private EmbedCondition condition;

    @JsonIgnore
    private IExpression<Object> conditionExpression;

    private AggregationFunctionType functionType;

    private String expr;

    public static AggregationComputeField build(Map<String,Object> fieldMap){
        AggregationComputeField computeField = new AggregationComputeField();
        computeField.name = (String) fieldMap.get("name");
        computeField.comment = (String) fieldMap.get("comment");
        computeField.code = (String) fieldMap.get("code");
        if(fieldMap.containsKey("valueType")){
            computeField.valueType = FieldValueType.valueOf((String) fieldMap.get("valueType"));
        }
        computeField.functionType = AggregationFunctionType.valueOf((String) fieldMap.get("functionType"));
        Map<String,Object> sf = (Map<String, Object>) fieldMap.get("summaryField");
        if(sf != null){
            computeField.summaryField = BeanUtils.toBean(sf,NodeParamField.class);
        }
        Map<String,Object> ct = (Map<String, Object>) fieldMap.get("condition");
        if(ct!=null){
            computeField.condition = JSONUtils.parseObject(ct, EmbedCondition.class);
        }

        return computeField;
    }


    @Override
    public void initialize() {
        if (condition != null) {
            condition.initialize();
            expr = condition.getStatement();
            if(StringUtils.isNotEmpty(expr)){
                conditionExpression = new AviatorExpression(expr, null);
            }
        }
        log.debug("field,{},{} expression:{},", code, name, expr);
    }

    public Object compute(Collection<Map<String, Object>> items) {
        Object val = null;
        try {
            switch (functionType) {
                case AVG:
                    val = avg(items);
                    break;
                case MAX:
                    val = max(items);
                    break;
                case MIN:
                    val = min(items);
                    break;
                case SUM:
                    val = sum(items);
                    break;
                case COUNT:
                    val = count(items);
                    break;
                case DISCOUNT:
                    val = distinctCount(items);
                    break;
                case STD_DEV:
                    val = stdDev(items);
                    break;
                case VARIANCE:
                    val = variance(items);
                    break;
                case POP_STD_DEV:
                    val = popStdDev(items);
                    break;
                case POP_VARIANCE:
                    val = popVariance(items);
                    break;
                case KURT:
                    val = kurtosis(items);
                    break;
                case MAD:
                    val = meanAbsDev(items);
                    break;
                case SEM:
                    val = stdErrOfMean(items);
                    break;
                case SOS:
                    val = sumOfSquares(items);
                    break;
                case MODE:
                    val = mode(items);
                    break;
                case PROD:
                    val = product(items);
                    break;
                case SKEW:
                    val = skewness(items);
                    break;
                case SEMI_VAR:
                    val = semiVariance(items);
                    break;
                case SumOfLogs:
                    val = sumOfLogs(items);
                    break;
                case GeometricMean:
                    val = geometricMean(items);
                    break;
                case MEDIAN:
                    val = median(items);
                    break;
                case PERCENTILE:
                default:
            }
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return val;
    }

    double semiVariance(Collection<Map<String, Object>> items){
        SemiVariance semiVariance = new SemiVariance();
        return semiVariance.evaluate(toDoubleArray(items));
    }

    double sumOfLogs(Collection<Map<String, Object>> items){
        return stcCompute(new SumOfLogs(),items);
    }


    double geometricMean(Collection<Map<String, Object>> items){
        return stcCompute(new GeometricMean(),items);
    }

    double sumOfSquares(Collection<Map<String, Object>> items){
        return stcCompute(new SumOfSquares(),items);
    }


    double kurtosis(Collection<Map<String, Object>> items){
        return stcCompute(new Kurtosis(),items);
    }


    double skewness(Collection<Map<String, Object>> items){
        return stcCompute(new Skewness(),items);
    }

    double product(Collection<Map<String, Object>> items){
        return stcCompute(new Product(),items);
    }


    double meanAbsDev(Collection<Map<String, Object>> items){
        Mean mean = new Mean();
        for (Map<String, Object> item : items) {
            if(this.matchItem(item)){
                mean.increment(value(item));
            }
        }
        return mean.getResult()/mean.getN();
    }

    double stdErrOfMean(Collection<Map<String, Object>> items){
        StandardDeviation stdDev = new StandardDeviation();
        for (Map<String, Object> item : items) {
            if(this.matchItem(item)){
                stdDev.increment(value(item));
            }
        }
        return stdDev.getResult() / Math.sqrt(stdDev.getN());
    }


    double mode(Collection<Map<String, Object>> items){
        return 0d;
    }

    double popStdDev(Collection<Map<String, Object>> items){
        return popStdDev(toDoubleArray(items));
    }

    /**
     * population standard deviation 总体标准差
     *
     * @param data
     * @return
     */
    double popStdDev(double[] data) {
        return Math.sqrt(popVariance(data));
    }

    private Object stdDev(Collection<Map<String, Object>> items){
        return stcCompute(new StandardDeviation(),items);
    }

    /**
     * standard deviation 样本标准差
     */
    double stdDev(double[] data) {
        double std_dev;
        std_dev = Math.sqrt(variance(data));
        return std_dev;
    }

    /**
     * population variance
     */
    private Double popVariance(Collection<Map<String, Object>> items) {
        return popVariance(toDoubleArray(items));
    }

    private Double popVariance(double[] data) {
        double variance = 0;
        double avg = Arrays.stream(data).average().orElse(0);
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - avg), 2));
        }
        variance = variance / data.length;
        return variance;
    }

    private Object variance(Collection<Map<String, Object>> items){
        return stcCompute(new Variance(), items);
    }


    /**
     * 样本方差
     *
     * @param data
     * @return
     */
    double variance(double[] data) {
        double variance = 0;
        double avg = Arrays.stream(data).average().orElse(0);
        for (int i = 0; i < data.length; i++) {
            variance = variance + (Math.pow((data[i] - avg), 2));
        }
        variance = variance / (data.length - 1);
        return variance;
    }


    private double stcCompute(AbstractStorelessUnivariateStatistic statistic,Collection<Map<String, Object>> items){
        for (Map<String, Object> item : items) {
            if(this.matchItem(item)){
                statistic.increment(this.value(item));
            }
        }
        return statistic.getResult();
    }

    private Object count(Collection<Map<String, Object>> items) {
        return items.stream().filter(this::matchItem).count();
    }

    private Object distinctCount(Collection<Map<String, Object>> items) {
        return items.stream().filter(this::matchItem)
                .map(item -> item.get(summaryField.getCode()))
                .distinct().count();
    }

    private Object min(Collection<Map<String, Object>> items) {
        return items.stream()
                .filter(this::matchItem)
                .map(item -> item.get(summaryField.getCode()))
                .min((o1, o2) -> {
                    if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(o2);
                    }
                    return 0;
                }).orElse(null);
    }

    private Object median(Collection<Map<String, Object>> items) {
        Median median = new Median();
        return median.evaluate(toDoubleArray(items));
    }

    private double[] toDoubleArray(Collection<Map<String,Object>> items){
        return items.stream()
                .filter(this::matchItem)
                .mapToDouble(this::value).toArray();
    }

    private Object max(Collection<Map<String, Object>> items) {
        return items.stream()
                .filter(this::matchItem)
                .map(item -> item.get(summaryField.getCode()))
                .max((o1, o2) -> {
                    if (o1 instanceof Comparable && o2 instanceof Comparable) {
                        return ((Comparable) o1).compareTo(o2);
                    }
                    return 0;
                }).orElse(null);
    }

    private Object avg(Collection<Map<String, Object>> items) {
        return stcCompute(new Mean(),items);
    }

    private Object sum(Collection<Map<String, Object>> items) {
        Sum sum = new Sum();
        Double sumVal = 0d;
        for (Map<String, Object> item : items) {
            if(this.matchItem(item)){
                sum.increment(value(item));
            }
        }
        sumVal = sum.getResult();

        Object v = 0;
        switch (summaryField.getValueType()) {
            case DECIMAL:
                v = BigDecimal.valueOf(sumVal);
                break;
            case DOUBLE:
                v = sum;
                break;
            case INTEGER:
            case NUMBER:
                v = sumVal.intValue();
                break;
            default:
                double e = sumVal - sumVal.intValue();
                if (e == 0) {
                    v = sumVal.intValue();
                }
        }
        return v;
    }


    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", valueType=").append(valueType);
        sb.append(", expr='").append(expr).append('\'');
        sb.append('}');
        return sb.toString();
    }

    private boolean matchItem(Map<String, Object> item) {
        if (conditionExpression != null) {
            return (Boolean) conditionExpression.execute(item);
        } else {
            return true;
        }
    }

    private Double value(Map<String, Object> item) {
        try {
            Object v = item.getOrDefault(summaryField.getCode(), 0d);
            if (v instanceof Double) {
                return (double) v;
            }else if(v instanceof Number){
                return ((Number) v).doubleValue();
            } else if(v !=null && v.toString().matches("[\\d]+[.]*[\\d]+")){
                return Double.parseDouble(v.toString());
            }else{
                return 0d;
            }
        }catch (Exception e){
            return 0d;
        }
    }


}
