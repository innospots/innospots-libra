package io.innospots.base.function.aggregation;

import io.innospots.base.model.field.ParamField;
import io.innospots.base.re.IExpression;

import java.util.Collection;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/22
 */
public interface IAggregationFunction {


    Object compute(Collection<Map<String, Object>> items);

    boolean match(Map<String, Object> item);

    String summaryFieldCode();

    default String weightFieldCode() {
        return null;
    }

    void initialize(ParamField summaryField, ParamField weightField, IExpression<?> condition);


    /**
     * convert the value to double, which this is the summary field in the item
     *
     * @param item
     * @return
     */
    default Double value(Map<String, Object> item) {
        double v = v(item, summaryFieldCode());
        double w = 1d;
        if (weightFieldCode() != null) {
            w = v(item, weightFieldCode());
        }
        return v * w;
    }

    default Double v(Map<String, Object> item, String field) {
        try {
            Object v = item.getOrDefault(field, 0d);
            if (v instanceof Double) {
                return (double) v;
            } else if (v instanceof Number) {
                return ((Number) v).doubleValue();
            } else if (v != null && v.toString().matches("[\\d]+[.]*[\\d]+")) {
                return Double.parseDouble(v.toString());
            } else {
                return 0d;
            }
        } catch (Exception e) {
            return 0d;
        }
    }
}
