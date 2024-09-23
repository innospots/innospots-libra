package io.innospots.connector.ai.aliyun.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.constant.PathConstant;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.connector.ai.aliyun.operator.AliTtsOperator;
import lombok.extern.slf4j.Slf4j;

import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class AliTtsConnectorMinder extends AliAiConnectorMinder {

    @Override
    public void open() {
        this.models = new String[]{"cosyvoice-v1,商用tts"};
        if (this.aliyunAiOperator == null) {
            this.aliyunAiOperator = new AliTtsOperator(this.connectionCredential.v(API_KEY), options());
        }
    }

    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        SchemaRegistry registry = super.schemaRegistryByCode(registryCode);
        registry.addField(new SchemaField().fill("text","text",
                FieldValueType.STRING, FieldScope.BODY));
        registry.addField(new SchemaField().fill("voice","voice",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("format","format",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("text_type","text_type",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("volume","volume",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("rate","rate",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("pitch","pitch",
                FieldValueType.DOUBLE, FieldScope.PARAM));
        return registry;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setTargetName("cosyvoice-v1");
        baseRequest.addQuery("voice", "loongstella");
        baseRequest.setBody("语音");
        try {
            AliTtsOperator aliTtsOperator = new AliTtsOperator(connectionCredential.v(API_KEY), options(connectionCredential));
            DataBody<ByteBuffer> dataBody = aliTtsOperator.execute(baseRequest);
        } catch (Exception e) {
            log.error(e.getMessage());
            return false;
        }
        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        BaseRequest baseRequest = new BaseRequest();
        baseRequest.setTargetName(tableName);
        baseRequest.addQuery("voice", "loongstella");
        baseRequest.setBody("语音");
        ExecutionResource executionResource = null;
        try {
            AliTtsOperator aliTtsOperator = new AliTtsOperator(connectionCredential.v(API_KEY), options(connectionCredential));
            DataBody<ByteBuffer> dataBody = aliTtsOperator.execute(baseRequest);
            Path outFile = Files.createTempFile(tableName + "_",".mp3");
            Files.write(outFile, dataBody.getBody().array());
            executionResource = ExecutionResource.buildResource(outFile.toFile(),false, PathConstant.RESOURCE_PATH);
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return executionResource;
    }
}
