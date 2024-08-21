package io.innospots.workflow.node.app.utils;

import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.node.app.compute.AggregationComputeField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author Smars
 * @date 2023/8/19
 */
public class AppNodeUtils {

    public static ArrayListValuedHashMap<String, Map<String, Object>> groupItems(NodeExecution nodeExecution,List<NodeParamField> dimensionFields){
        ArrayListValuedHashMap<String, Map<String, Object>> groupItems = new ArrayListValuedHashMap<>();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                String key = dimensionFields.stream().map(f-> String.valueOf(item.get(f.getCode()))).collect(Collectors.joining("~"));
                groupItems.put(key, item);
            }//end for item
        }//end for execution input
        return groupItems;
    }

    public static  List<Map<String, Object>> computeAggregateFields(ArrayListValuedHashMap<String, Map<String, Object>> groupItems,
                                                             List<AggregationComputeField> computeFields,
                                                             List<NodeParamField> dimensionFields
                                                             ){
        List<Map<String, Object>> items = new ArrayList<>();
        for (Map.Entry<String, Collection<Map<String, Object>>> entry : groupItems.asMap().entrySet()) {
            Map<String, Object> aggItem = new HashMap<>();
            //item.put(dimensionField.getCode(), entry.getKey());
            for (AggregationComputeField computeField : computeFields) {
                aggItem.put(computeField.getCode(), computeField.compute(entry.getValue()));
            }
            String[] dims = entry.getKey().split("~");
            for (int i = 0; i < dims.length; i++) {
                aggItem.put(dimensionFields.get(i).getCode(),dims[i]);
            }
            items.add(aggItem);
        }//end for
        return items;
    }

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
