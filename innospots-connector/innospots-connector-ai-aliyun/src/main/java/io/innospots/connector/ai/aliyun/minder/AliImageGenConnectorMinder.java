package io.innospots.connector.ai.aliyun.minder;

import io.innospots.base.connector.credential.model.ConnectionCredential;
import io.innospots.base.connector.schema.model.SchemaField;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.request.BaseRequest;
import io.innospots.base.model.field.FieldScope;
import io.innospots.base.model.field.FieldValueType;
import io.innospots.connector.ai.aliyun.operator.AliImageGenOperator;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/9/20
 */
@Slf4j
public class AliImageGenConnectorMinder extends AliAiConnectorMinder {
    @Override
    public void open() {
        this.models = new String[]{
                "wanx-v1,通义万相",
                "wanx-style-cosplay-v1,Cosplay动漫人物生成",
                "wanx-poster-generation-v1,创意海报",
//                "wanx-virtualmodel,虚拟模特",
//                "virtualmodel-v2,虚拟模特v2",
//                "image-erase-completion",
                "wanx-sketch-to-image-lite,通义涂鸦",
//                "image-out-painting,图像扩展",
                "wanx-x-painting,局部重绘"
        };
        if (this.aliyunAiOperator == null) {
            this.aliyunAiOperator = new AliImageGenOperator(this.connectionCredential.v(API_KEY), options());
        }
    }

    @Override
    public SchemaRegistry schemaRegistryByCode(String registryCode) {
        SchemaRegistry registry = super.schemaRegistryByCode(registryCode);
        registry.addField(new SchemaField().fill("prompt","prompt",
                FieldValueType.STRING, FieldScope.BODY));
        registry.addField(new SchemaField().fill("size","size",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("style","style",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("negativePrompt","negativePrompt",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("refImage","refImage",
                FieldValueType.STRING, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("sketchImageUrl","sketchImageUrl",
                FieldValueType.STRING, FieldScope.PARAM));
        SchemaField sf = new SchemaField().fill("extraInputs","extraInputs",
                FieldValueType.MAP, FieldScope.BODY);
        registry.addField(sf);
        List<SchemaField> subFields = new ArrayList<>();
        subFields.add(new SchemaField().fill("template_image_url","template_image_url",
                FieldValueType.STRING,FieldScope.PARAM));
        subFields.add(new SchemaField().fill("face_image_url","face_image_url",
                FieldValueType.STRING,FieldScope.PARAM));
        subFields.add(new SchemaField().fill("model_index","model_index",
                FieldValueType.INTEGER,FieldScope.PARAM));
        subFields.add(new SchemaField().fill("base_image_url","base_image_url",
                FieldValueType.STRING,FieldScope.PARAM));
        subFields.add(new SchemaField().fill("mask_image_url","mask_image_url",
                FieldValueType.STRING,FieldScope.PARAM));
        sf.setSubFields(subFields);

        registry.addField(new SchemaField().fill("seed","seed",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("steps","steps",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("scale","scale",
                FieldValueType.INTEGER, FieldScope.PARAM));
        registry.addField(new SchemaField().fill("n","n",
                FieldValueType.INTEGER, FieldScope.PARAM));

        return registry;
    }

    @Override
    public Object testConnect(ConnectionCredential connectionCredential) {
        AliImageGenOperator imageGenOperator = new AliImageGenOperator(connectionCredential.v("API_KEY"),options(connectionCredential));
        Map<String,Object> body = new HashMap<>();
        body.put("prompt","风景");
        body.put("n",1);
        body.put("size","128*128");
        try{
            BaseRequest baseRequest = testRequest(connectionCredential,"wanx-v1",body,null);
            DataBody dataBody = imageGenOperator.execute(baseRequest);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return false;
        }

        return true;
    }

    @Override
    public Object fetchSample(ConnectionCredential connectionCredential, String tableName) {
        AliImageGenOperator imageGenOperator = new AliImageGenOperator(connectionCredential.v("API_KEY"),options(connectionCredential));
        Map<String,Object> body = new HashMap<>();
        body.put("prompt","风景");
        body.put("n",1);
        body.put("size","1024*1024");
        DataBody dataBody = null;
        try{
            BaseRequest baseRequest = testRequest(connectionCredential,"wanx-v1",body,null);
            dataBody = imageGenOperator.execute(baseRequest);
        }catch (Exception e){
            log.error(e.getMessage(),e);
            return null;
        }
        return dataBody.getBody();
    }
}
