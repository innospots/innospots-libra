package io.innospots.app.visitor.operator;

import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.base.utils.http.HttpData;
import io.innospots.connector.core.minder.DataConnectionMinderManager;
import io.innospots.connector.core.minder.IDataConnectionMinder;
import io.innospots.connector.core.schema.model.SchemaField;
import io.innospots.connector.core.schema.model.SchemaRegistry;
import io.innospots.connector.core.schema.model.SchemaRegistryType;
import io.innospots.base.data.body.DataBody;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.data.enums.ApiMethod;
import io.innospots.base.data.operator.IOperator;
import io.innospots.base.data.request.ItemRequest;
import io.innospots.base.exception.ResourceException;
import io.innospots.base.model.Pair;
import io.innospots.base.model.response.R;
import io.innospots.base.model.response.ResponseCode;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/28
 */
@Component
public class AppDataOperator {

    private AppDefinitionOperator appDefinitionOperator;

    private DataConnectionMinderManager connectionMinderManager;


    public AppDataOperator(AppDefinitionOperator appDefinitionOperator, DataConnectionMinderManager connectionMinderManager) {
        this.appDefinitionOperator = appDefinitionOperator;
        this.connectionMinderManager = connectionMinderManager;
    }

    public Map<String, Object> show(String appKey, String registryId, Map<String, Object> params) {
        Pair<IOperator,ItemRequest> pair = buildDataOperator(appKey, registryId, params);
        IOperator dataOperator = pair.getLeft();
        ItemRequest itemRequest = pair.getRight();
        DataBody<Map<String, Object>> dataBody = dataOperator.execute(itemRequest);
        return dataBody.getBody();
    }

    private Pair<IOperator,ItemRequest> buildDataOperator(String appKey, String registryId, Map<String, Object> params){
        AppDefinition appDefinition = appDefinitionOperator.getAppDefinition(appKey);
        if (appDefinition == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "appKey", appKey);
        }
        SchemaRegistry schemaRegistry = getAppInSchemaRegistry(appDefinition, registryId);
        IDataConnectionMinder connectionMinder = connectionMinderManager.getMinder(schemaRegistry);
        IOperator dataOperator = connectionMinder.buildOperator();
        ItemRequest itemRequest = buildRequest(schemaRegistry, params);
        return Pair.of(dataOperator,itemRequest);
    }

    public PageBody<Map<String, Object>> page(String appKey, String registryId, Map<String, Object> params) {
        Pair<IOperator,ItemRequest> pair = buildDataOperator(appKey, registryId, params);
        IOperator dataOperator = pair.getLeft();
        ItemRequest itemRequest = pair.getRight();
        return dataOperator.executePage(itemRequest);
    }

    public List<Object> list(String appKey, String registryId, Map<String, Object> params) {
        Pair<IOperator,ItemRequest> pair = buildDataOperator(appKey, registryId, params);
        IOperator dataOperator = pair.getLeft();
        ItemRequest itemRequest = pair.getRight();
        return dataOperator.executePage(itemRequest).getList();
    }

    public Object submit(String appKey, String registryId, Map<String, Object> body) {
        Pair<IOperator,ItemRequest> pair = buildDataOperator(appKey, registryId, body);
        IOperator dataOperator = pair.getLeft();
        ItemRequest itemRequest = pair.getRight();
        DataBody<?> dataBody = dataOperator.execute(itemRequest);
        return processResponse(dataBody.getBody());
    }

    private Object processResponse(Object response){
        if(response instanceof HttpData httpData){
            if(httpData.getStatus() >= 300){
                return R.fail(ResponseCode.FAIL,httpData.getMessage());
            }else{
                response = httpData.getBody();
            }
        }
        if(response instanceof Map<?,?> m){
//            Map<String,Object> m = (Map<String, Object>) response;
            if(m.containsKey("code") && m.containsKey("detail")){
                return m;
            }
            return R.success(m);
        }else if(response instanceof R<?>){
            return response;
        }

        return R.success(response);
    }


    private ItemRequest buildRequest(SchemaRegistry schemaRegistry, Map<String, Object> params) {
        Map<String, Object> input = new HashMap<>();
        Integer page = null;
        Integer size = null;
        ItemRequest itemRequest = new ItemRequest();
        if (CollectionUtils.isNotEmpty(schemaRegistry.getSchemaFields())) {
            for (SchemaField schemaField : schemaRegistry.getSchemaFields()) {
                input.put(schemaField.getCode(), params.get(schemaField.getDefaultValue()));
            }
        } else {
            page = (Integer) params.get("page");
            size = (Integer) params.get("size");
            if (page != null) {
                params.remove("page");
                itemRequest.setPage(page);
            }
            if (size != null) {
                params.remove("size");
                itemRequest.setSize(size);
            }
        }

        itemRequest.setOperation(schemaRegistry.dataOperation());
        itemRequest.setConnectorName(schemaRegistry.getConnectorName());
        itemRequest.setCredentialKey(schemaRegistry.getCredentialKey());
        ApiMethod apiMethod = ApiMethod.valueOf(schemaRegistry.dataOperation());

        if (apiMethod == ApiMethod.POST) {
            itemRequest.setBody(input);
        } else if (apiMethod == ApiMethod.GET) {
            itemRequest.setQuery(input);
        }
        if(schemaRegistry.getRegistryType() == SchemaRegistryType.WORKFLOW){
            itemRequest.addQuery("respType","app");
        }
        return itemRequest;
    }


    private SchemaRegistry getAppInSchemaRegistry(AppDefinition appDefinition, String schemaRegistryId) {
        if (appDefinition == null || appDefinition.getResources() == null ||
                CollectionUtils.isEmpty(appDefinition.getResources().getRegistries())) {
            return null;
        }

        return appDefinition.getResources()
                .getRegistries().stream()
                .filter(r -> Objects.equals(r.getRegistryId(), schemaRegistryId))
                .findFirst().orElse(null);
    }

}
