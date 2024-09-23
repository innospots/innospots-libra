package io.innospots.workflow.node.app.llm;

import io.innospots.base.condition.Factor;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.base.utils.BeanUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import reactor.core.publisher.Flux;

import java.util.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/18
 */
@Slf4j
public class AliAiNode extends BaseNodeExecutor {

    public static final String FIELD_CREDENTIAL_KEY = "credential_key";

    public static final String FIELD_MODEL_NAME = "model_name";

    public static final String FILED_MAPPING = "mapping_fields";

    public static final String FIELD_LLM_EXECUTE_MODE = "execute_mode";

    protected String credentialKey;

    protected String modelName;

    protected LlmExecuteMode executeMode;

    protected List<Factor> mappingFields;

    protected IOperator llmDataOperator;

    protected List<SchemaField> schemaFields;

    @Override
    protected void initialize() {
        this.credentialKey = this.validString(FIELD_CREDENTIAL_KEY);
        this.modelName = this.valueString(FIELD_MODEL_NAME);
        String eMode = this.valueString(FIELD_LLM_EXECUTE_MODE);
        if(eMode!=null){
            this.executeMode = LlmExecuteMode.valueOf(eMode);
        }else{
            this.executeMode = LlmExecuteMode.SYNC;
        }

        IDataConnectionMinder dataConnectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        llmDataOperator = dataConnectionMinder.buildOperator();
        List<Map<String, Object>> columnFieldMapping = valueMapList(FILED_MAPPING);
        if (columnFieldMapping != null) {
            mappingFields = BeanUtils.toBean(columnFieldMapping, Factor.class);
        }
        SchemaRegistry schemaRegistry = dataConnectionMinder.schemaRegistryByCode(this.modelName);
        this.schemaFields = schemaRegistry.getSchemaFields();
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        BaseRequest<?> llmRequest = buildRequest(nodeExecution);
        switch (executeMode) {
            case STREAM:
                StringBuilder content = new StringBuilder();
                Flux fluxStream = llmDataOperator.executeStream(llmRequest);
                if (fluxStream == null) {
                    return;
                }
                fluxStream.doOnNext(b -> {
                    Map<String, Object> m = (Map<String, Object>) b;
                    flowLogger.item(nodeExecution.getFlowExecutionId(), m);
                    content.append(m.get("content"));
                }).blockLast();
                processOutput(nodeExecution, content.toString(), nodeOutput);
                break;
            case SYNC:
                DataBody dataBody = llmDataOperator.execute(llmRequest);
                Object resp = dataBody.getBody();
                processOutput(nodeExecution, resp, nodeOutput);
                break;
            default:
        }
    }


    @Override
    protected void processOutput(NodeExecution nodeExecution, Object result, ExecutionOutput nodeOutput) {
        if (result instanceof Map) {
            nodeOutput.addResult((Map<String, Object>) result);
        } else if (result instanceof Collection<?>) {
            nodeOutput.addResult((Collection<Map<String, Object>>) result);
        } else if (result instanceof String) {
            try {
                String r = (String) result;
                if (r.startsWith("{") && r.endsWith("}")) {
                    Map m = JSONUtils.toMap(r);
                    if (m != null) {
                        nodeOutput.addResult(m);
                    } else {
                        nodeOutput.addResult("response", r);
                    }
                } else if (r.startsWith("[") && r.endsWith("]")) {
                    Collection collection = JSONUtils.toList(r, Map.class);
                    if (CollectionUtils.isNotEmpty(collection)) {
                        nodeOutput.addResult(collection);
                    } else {
                        nodeOutput.addResult("response", r);
                    }
                } else {
                    nodeOutput.addResult("response", result);
                }
            } catch (Exception e) {
                log.error(e.getMessage());
                nodeOutput.addResult("response", result);
            }
        }
    }

    protected BaseRequest buildRequest(NodeExecution nodeExecution) {
        List<Map<String, Object>> items = new ArrayList<>();
        BaseRequest request = new BaseRequest<>();
        request.setTargetName(this.modelName);
        request.setOperation(executeMode.name());

        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            int pos = 0;
            StringBuilder content = new StringBuilder();
            for (Map<String, Object> item : executionInput.getData()) {
                Map<String, Object> itemData = new LinkedHashMap<>();

                Map<String, Object> mapInput = new HashMap<>();
                if (CollectionUtils.isNotEmpty(mappingFields)) {
                    for (Factor factor : mappingFields) {
                        Object value = factor.value(item);
                        if (value != null) {
                            mapInput.put(factor.getCode(), value);
                        }
                    }
                }

                if (this.schemaFields != null) {
                    for (SchemaField schemaField : schemaFields) {
                        Object value = null;
                        if (schemaField.getValueType() == FieldValueType.MAP &&
                                schemaField.getSubFields() != null) {
                            Map<String, Object> subMap = new HashMap<>();
                            for (SchemaField subField : schemaField.getSubFields()) {
                                Object subValue = findValue(item, mapInput, subField);
                                if (subValue != null) {
                                    subMap.put(subField.getCode(), subValue);
                                }
                            }//end sub fields
                            if (!subMap.isEmpty()) {
                                value = subMap;
                            }
                        } else {
                            value = findValue(item, mapInput, schemaField);
                        }

                        if (value != null) {
                            if (schemaField.getFieldScope() == FieldScope.BODY) {
                                itemData.put(schemaField.getCode(), value);
                            } else {
                                request.addQuery(schemaField.getConfig(), value);
                            }
                        } else {
                            item.forEach((k, v) -> {
                                content.append(k).append(": ");
                                content.append(v).append("\n");
                            });
                        }
                    }//end for schema fields
                }//end if schema fields not null
                if (!content.isEmpty()) {
                    itemData.put("content", content);
                }
                if (executionInput.getResources() != null && executionInput.getResources().size() > pos) {
                    itemData.put("resources", executionInput.getResources().get(pos));
                }
                items.add(itemData);
                pos++;
            }//end input items
        }

        if (items.size() == 1) {
            request.setBody(items.get(0));
        } else {
            request.setBody(items);
        }
        return request;
    }

    private Object findValue(Map<String, Object> input, Map<String, Object> mapInput, SchemaField schemaField) {
        Object value = this.value(schemaField.getCode());
        if (value == null && !mapInput.isEmpty()) {
            value = mapInput.get(schemaField.getCode());
        }
        if (value == null) {
            value = input.getOrDefault(schemaField.getCode(), schemaField.getDefaultValue());
        }
        return value;
    }

    enum LlmExecuteMode {
        STREAM,
        SYNC;
    }

}
