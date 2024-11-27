package io.innospots.approve.node;

import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@Slf4j
public class ApproveStartNode extends BaseNodeExecutor {

    private static final String FIELDS = "input_fields";

    private List<NodeParamField> inputFields;

    @Override
    protected void initialize() {
        inputFields = NodeInstanceUtils.buildParamFields(ni, FIELDS);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }


    @Override
    protected Object processItem(Map<String, Object> item) {
        if (CollectionUtils.isEmpty(inputFields)) {
            return item;
        }
        Map<String, Object> nItem = new LinkedHashMap<>();
        for (NodeParamField inputField : inputFields) {
            Object v = item != null ? item.getOrDefault(inputField.getCode(), inputField.getValue()) : inputField.getValue();
            if (v != null && StringUtils.isNotEmpty(v.toString())) {
                nItem.put(inputField.getCode(), v);
            }
        }//end inputFields
        if (log.isDebugEnabled()) {
            log.debug("start node input:{}", nItem);
        }
        return nItem;
    }
}
