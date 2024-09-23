package io.innospots.connector.ai.aliyun.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.minder.BaseDataConnectionMinder;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.connector.ai.aliyun.operator.AliImageRecOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class AliImageRecConnectorMinder extends AliAiConnectorMinder {


    @Override
    public void open() {
        this.models = new String[]{
                "qwen-vl-plus,商用加强",
                "qwen-vl-max,商用最优",
                "qwen-vl-v1,开源v1",
                "qwen-vl-chat-v1,开源chat"
        };
        if (this.aliyunAiOperator == null) {
            this.aliyunAiOperator = new AliImageRecOperator(this.connectionCredential.v(API_KEY), options());
        }
    }

    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        SchemaRegistry registry = super.schemaRegistryByCode(registryCode);
        registry.addField(new SchemaField().fill("image","image",
                FieldValueType.STRING, FieldScope.BODY));
        registry.addField(new SchemaField().fill("text","text",
                FieldValueType.STRING, FieldScope.BODY));
        registry.addField(new SchemaField().fill("incrementalOutput","incrementalOutput",
                FieldValueType.BOOLEAN, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("temperature","temperature",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("seed","seed",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("topP","topP",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("topK","topK",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("maxLength","maxLength",
                FieldValueType.INTEGER, FieldScope.PARAM));
        return registry;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        AliImageRecOperator imageRecOperator = new AliImageRecOperator(connectionCredential.v(API_KEY), options(connectionCredential));
        BaseRequest request = new BaseRequest();
        request.setTargetName("qwen-vl-v1");
        request.addQuery("text","识别图像中的文字");
        try {
            DataBody dataBody = imageRecOperator.execute(request);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return false;
        }
        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        return testConnect(connectionCredential);
    }
}
