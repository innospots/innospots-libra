package io.innospots.workflow.node.app.utils;

import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.node.app.compute.AggregationComputeField;
import org.apache.commons.collections4.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2023/8/19
 */
public class AppNodeUtils {

    public static List<AggregationComputeField> buildAggregationComputeFields(NodeInstance nodeInstance, String fieldName) {
        List<Map<String, Object>> fieldMaps = (List<Map<String, Object>>) nodeInstance.value(fieldName);

        List<AggregationComputeField> computeFields = new ArrayList<>();
        computeFields = BeanUtils.toBean(fieldMaps, AggregationComputeField.class);
        computeFields.forEach(AggregationComputeField::initialize);

        /*
        if (CollectionUtils.isEmpty(fieldMaps)) {
            return computeFields;
        }
        for (Map<String, Object> fieldMap : fieldMaps) {
            AggregationComputeField cf = AggregationComputeField.build(fieldMap);
            cf.initialize();
            computeFields.add(cf);
        }
         */
        return computeFields;
    }
}
