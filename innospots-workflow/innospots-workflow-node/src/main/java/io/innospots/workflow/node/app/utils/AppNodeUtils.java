package io.innospots.workflow.node.app.utils;

import io.innospots.base.condition.EmbedCondition;
import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Mode;
import io.innospots.base.exception.ConfigException;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.script.IScriptExecutor;
import io.innospots.base.script.aviator.AviatorExpressionExecutor;
import io.innospots.base.utils.BeanUtils;
import io.innospots.base.utils.Initializer;
import io.innospots.workflow.core.instance.model.NodeInstance;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.node.app.execute.AggregationComputeField;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.Collections;
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
        if (CollectionUtils.isEmpty(fieldMaps)) {
            return computeFields;
        }
        for (Map<String, Object> fieldMap : fieldMaps) {
            AggregationComputeField cf = AggregationComputeField.build(fieldMap);
            cf.initialize();
            computeFields.add(cf);
        }
        return computeFields;
    }
}
