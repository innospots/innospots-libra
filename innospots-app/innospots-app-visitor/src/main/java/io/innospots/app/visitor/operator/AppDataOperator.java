package io.innospots.app.visitor.operator;

import io.innospots.app.core.model.AppDefinition;
import io.innospots.app.core.operator.AppDefinitionOperator;
import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.innospots.base.data.body.PageBody;
import io.innospots.base.exception.ResourceException;
import org.apache.commons.collections4.CollectionUtils;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/8/28
 */
public class AppDataOperator {

    private AppDefinitionOperator appDefinitionOperator;


    public Map<String, Object> show(String appKey, String registryId, Map<String, Object> params) {
        AppDefinition appDefinition = appDefinitionOperator.getAppDefinition(appKey);
        if (appDefinition == null) {
            throw ResourceException.buildNotExistException(this.getClass(), "appKey", appKey);
        }

        return null;
    }

    public PageBody<Map<String, Object>> page(String appKey, String registryId, Map<String, Object> params) {

        return null;
    }

    public List<Map<String, Object>> list(String appKey, String registryId, Map<String, Object> params) {

        return null;
    }

    public Map<String, Object> submit(String appKey, String registryId, Map<String, Object> body) {

        return null;
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
