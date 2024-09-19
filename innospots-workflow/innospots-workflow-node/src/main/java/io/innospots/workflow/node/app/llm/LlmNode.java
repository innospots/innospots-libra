package io.innospots.workflow.node.app.llm;

import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.json.JSONUtils;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.node.field.NodeParamField;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
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
public class LlmNode extends BaseNodeExecutor {

    public static final String FIELD_CREDENTIAL_KEY = "credential_key";

    public static final String FIELD_MODEL_NAME = "model_name";
    public static final String FIELD_CONTENT = "content_fields";

    public static final String FIELD_LLM_EXECUTE_MODE = "execute_mode";
    public static final String FIELD_TEMPERATURE = "temperature";
    public static final String FIELD_FORMAT = "format";
    public static final String FIELD_TOP_K = "top_k";
    public static final String FIELD_TOP_P = "top_p";
    public static final String FIELD_SEED = "seed";
    public static final String FIELD_MAX_TOKENS = "max_tokens";

    private String credentialKey;

    private String modelName;

    private LlmExecuteMode executeMode;

    private List<NodeParamField> contentFields;

    private IOperator llmDataOperator;
    private Map<String, Object> options = new HashMap<>();

    @Override
    protected void initialize() {
        this.credentialKey = this.validString(FIELD_CREDENTIAL_KEY);
        this.modelName = this.valueString(FIELD_MODEL_NAME);
        this.executeMode = LlmExecuteMode.valueOf(this.valueString(FIELD_LLM_EXECUTE_MODE));
        IDataConnectionMinder dataConnectionMinder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        llmDataOperator = dataConnectionMinder.buildOperator();
        contentFields = NodeInstanceUtils.buildParamFields(ni, FIELD_CONTENT);
        this.fillOption(FIELD_FORMAT);
        this.fillOption(FIELD_TEMPERATURE);
        this.fillOption(FIELD_TOP_K);
        this.fillOption(FIELD_TOP_P);
        this.fillOption(FIELD_SEED);
        this.fillOption(FIELD_MAX_TOKENS);
    }

    private void fillOption(String key) {
        Object v = this.value(key);
        if (v != null) {
            options.put(key, v);
        }
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput nodeOutput = this.buildOutput(nodeExecution);
        BaseRequest llmRequest = buildRequest(nodeExecution);
        switch (executeMode) {
            case STREAM:
                StringBuilder content = new StringBuilder();
                Flux fluxStream = llmDataOperator.executeStream(llmRequest);
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
                    if(m!=null){
                        nodeOutput.addResult(m);
                    }else{
                        nodeOutput.addResult("response", r);
                    }
                } else if (r.startsWith("[") && r.endsWith("]")) {
                    Collection collection = JSONUtils.toList(r, Map.class);
                    if(CollectionUtils.isNotEmpty(collection)){
                        nodeOutput.addResult(collection);
                    }else{
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

    private BaseRequest buildRequest(NodeExecution nodeExecution) {
        List<Map<String, Object>> items = new ArrayList<>();
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            StringBuilder content = new StringBuilder();
            int pos = 0;
            for (Map<String, Object> item : executionInput.getData()) {
                if (CollectionUtils.isNotEmpty(contentFields)) {
                    for (NodeParamField field : contentFields) {
                        Object value = item.get(field.getCode());
                        content.append(field.getName()).append(": ");
                        content.append(value).append("\n");
                    }
                } else {
                    item.forEach((k, v) -> {
                        content.append(k).append(": ");
                        content.append(v).append("\n");
                    });
                }
                Map<String, Object> itemData = new LinkedHashMap<>();
                itemData.put("content", content);
                if (executionInput.getResources() != null && executionInput.getResources().size() > pos) {
                    itemData.put("resources", executionInput.getResources().get(pos));
                }
                items.add(itemData);
                pos++;
            }//end input items
        }
        BaseRequest request = new BaseRequest<>();
        request.setTargetName(this.modelName);
        request.setQuery(options);
        request.setBody(items);
        request.setOperation(executeMode.name());
        return request;
    }

    enum LlmExecuteMode {
        STREAM,
        SYNC;
    }

}
