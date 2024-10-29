package io.innospots.workflow.node.ai;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.innospots.connector.core.credential.model.ConnectionCredential;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.connector.core.minder.IDataConnectionMinder;
import io.innospots.base.json.JSONUtils;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.exception.NodeExecuteException;
import io.innospots.workflow.core.execution.model.ExecutionInput;
import io.innospots.workflow.core.execution.model.ExecutionOutput;
import io.innospots.workflow.core.execution.model.node.NodeExecution;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.utils.NodeInstanceUtils;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;

import java.util.Collection;
import java.util.Map;

import static io.innospots.workflow.node.ai.AiConstant.*;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/24
 */
@Setter
public abstract class AiBaseNode<Msg, Param> extends BaseNodeExecutor {

    protected ParamField promptField;

    protected String modelName;

    protected LlmExecuteMode executeMode;

    protected String apiKey;

    protected Integer maxTokens;

    protected Double temperature;

    protected Double topP;

    @Override
    protected void initialize() {
        String credentialKey = this.validString(FIELD_CREDENTIAL_KEY);
        IDataConnectionMinder minder = DataConnectionMinderManager.getCredentialMinder(credentialKey);
        ConnectionCredential connectionCredential = minder.connectionCredential();
        this.apiKey = connectionCredential.v("api_key");
        String eMode = this.valueString(FIELD_LLM_EXECUTE_MODE);
        if (eMode != null) {
            this.executeMode = LlmExecuteMode.valueOf(eMode);
        }
        this.temperature = this.valueDouble(FILED_TEMPERATURE);
        this.maxTokens = this.valueInteger(FILED_MAX_TOKENS);
        this.promptField = NodeInstanceUtils.buildParamField(this.ni, FILED_PROMPT);
        this.topP = this.valueDouble(FILED_TOP_P);

        this.modelName = this.valueString(FIELD_MODEL_NAME);
    }

    @Override
    public void invoke(NodeExecution nodeExecution) {
        ExecutionOutput executionOutput = this.buildOutput(nodeExecution);
        for (ExecutionInput executionInput : nodeExecution.getInputs()) {
            for (Map<String, Object> item : executionInput.getData()) {
                Object result = processItem(item, nodeExecution);
                processOutput(nodeExecution, result, executionOutput);
            }
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
                throw NodeExecuteException.buildException(this.getClass(),e, "process output error");
            }
        }
    }

    protected abstract Param buildParam(Map<String, Object> items,boolean stream);

    protected Param buildParam(Map<String, Object> items){
        return buildParam(items,false);
    }

    protected void fillOptions(Map<String, Object> item, Param param) {
        if (item != null) {
            BeanUtil.fillBeanWithMap(item, param, CopyOptions.create().ignoreError().setAutoTransCamelCase(false).ignoreNullValue());
        }
        if (this.ni.getData() != null) {
            BeanUtil.fillBeanWithMap(this.ni.getData(), param, CopyOptions.create().setAutoTransCamelCase(false).ignoreError().ignoreNullValue());
        }
    }

    protected abstract Msg buildMessage(Map<String, Object> inputItem);


}
