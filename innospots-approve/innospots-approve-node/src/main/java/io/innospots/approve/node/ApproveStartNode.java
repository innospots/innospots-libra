package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.enums.ApproveStatus;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/20
 */
@Slf4j
public class ApproveStartNode extends ApproveBaseNode {

    private static final String FIELDS = "input_fields";

    private List<NodeParamField> inputFields;

    @Override
    protected void initialize() {
        super.initialize();
        inputFields = NodeInstanceUtils.buildParamFields(ni, FIELDS);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        super.invoke(nodeExecution);
    }

    @Override
    public List<ParamField> inputFields() {
        if(inputFields!=null){
            return new ArrayList<>(inputFields);
        }
        return super.inputFields();
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
                fillApproveFlowInstance(inputField.getCode(),v.toString());
            }
            if(item!=null){
                item.remove(inputField.getCode());
            }
        }//end inputFields
        nItem.putAll(item);
        log.debug("start node input:{}", nItem);
        this.flowLogger.flowInfo("","","");
        return nItem;
    }

    private void fillApproveFlowInstance(String code,String v) {
        if (ApproveHolder.get() == null) {
            if (code.equals(ApproveConstant.APPROVE_INSTANCE_KEY)) {
                ApproveFlowInstance flowInstance = this.approveFlowInstanceOperator.findOne(v);
                if(flowInstance.getApproveStatus() != ApproveStatus.STARTING &&
                        flowInstance.getApproveStatus() != ApproveStatus.PROCESSING
                ){
                    throw ResourceException.buildStatusException(this.getClass(), "the Approve status is " +
                            flowInstance.getApproveStatus() + ", can't be executed", v);
                }
                ApproveHolder.set(flowInstance);
            }
        }
    }
}
