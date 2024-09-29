package io.innospots.workflow.node.ai.aliyun;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.DataConnectionMinderManager;
import io.innospots.base.connector.minder.IDataConnectionMinder;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.node.executor.BaseNodeExecutor;
import io.innospots.workflow.core.utils.NodeInstanceUtils;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/24
 */
public abstract class AliAiBaseNode<Msg, Param> extends BaseNodeExecutor {

    public static final String FIELD_CREDENTIAL_KEY = "credential_key";
    public static final String FIELD_MODEL_NAME = "model_name";
    public static final String FIELD_LLM_EXECUTE_MODE = "execute_mode";
    public static final String FILED_PROMPT = "prompt_txt";

    protected ParamField promptField;

    protected String modelName;

    protected LlmExecuteMode executeMode;

    protected String apiKey;

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

        this.promptField = NodeInstanceUtils.buildParamField(this.ni, FILED_PROMPT);

        this.modelName = this.valueString(FIELD_MODEL_NAME);
    }

    protected abstract Param buildParam(Map<String, Object> items);

    protected void fillOptions(Map<String, Object> item, Param param) {
        if (item != null) {
            BeanUtil.fillBeanWithMap(item, param, CopyOptions.create().ignoreError().ignoreNullValue());
        }
        if (this.ni.getData() != null) {
            BeanUtil.fillBeanWithMap(this.ni.getData(), param, CopyOptions.create().ignoreError().ignoreNullValue());
        }
    }

    protected abstract Msg buildMessage(Map<String, Object> inputItem);
}
