package io.innospots.approve.node;

import io.innospots.approve.core.constants.ApproveConstant;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.approve.core.operator.ApproveFlowInstanceOperator;
import io.innospots.approve.core.utils.ApproveHolder;
import io.innospots.base.data.operator.IDataOperator;
import io.innospots.base.json.JSONUtils;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.connector.core.minder.IDataConnectionMinder;
import io.innospots.connector.core.schema.model.SchemaField;
import io.innospots.connector.core.schema.model.SchemaRegistry;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/24
 */
public class ApproveFormDataNode extends BaseNodeExecutor {

    public static final String FIELD_CREDENTIAL_KEY = "credential_key";

    public static final String FIELD_TABLE_NAME = "table_name";

    private IDataOperator dataOperator;

    private String credentialKey;

    private String tableName;
    private ApproveFlowInstanceOperator approveFlowInstanceOperator;
    private SchemaRegistry schemaRegistry;

    @Override
    protected void initialize() {
        credentialKey = valueString(FIELD_CREDENTIAL_KEY);
        this.tableName = valueString(FIELD_TABLE_NAME);
        if (credentialKey != null) {
            IDataConnectionMinder connectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
            dataOperator = connectionMinder.buildOperator();
            schemaRegistry = connectionMinder.schemaRegistryByCode(tableName);
        }
        this.approveFlowInstanceOperator = getBean(ApproveFlowInstanceOperator.class);
    }

    @Override
    protected Object processItem(Map<String, Object> item, NodeExecution nodeExecution) {
        ApproveFlowInstance flowInstance = getApproveFlowInstance(item);
        Map<String, Object> formData = new HashMap<>();
        formData.put(ApproveConstant.APPROVE_INSTANCE_KEY, flowInstance.getApproveInstanceKey());
        for (SchemaField schemaField : schemaRegistry.getSchemaFields()) {
            Object v = formData.get(schemaField.getCode());
            if (v != null) {
                if (v instanceof Map || v instanceof List) {
                    formData.put(schemaField.getCode(), JSONUtils.toJsonString(v));
                } else {
                    formData.put(schemaField.getCode(), v);
                }
            }
        }
        dataOperator.insert(tableName, formData);
        return formData;
    }

    private ApproveFlowInstance getApproveFlowInstance(Map<String, Object> item) {
        ApproveFlowInstance approveFlowInstance = ApproveHolder.get();
        if (approveFlowInstance == null) {
            String approveInstanceId = (String) item.get(ApproveConstant.APPROVE_INSTANCE_KEY);
            approveFlowInstance = approveFlowInstanceOperator.findOne(approveInstanceId);
        }
        return approveFlowInstance;
    }
}
