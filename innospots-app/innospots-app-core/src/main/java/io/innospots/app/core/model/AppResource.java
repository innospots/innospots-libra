package io.innospots.app.core.model;

import io.innospots.base.connector.schema.model.SchemaRegistry;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/11
 */
@Getter
@Setter
@Schema(title = "app config, include page config, apis config, data view config")
public class AppResource {

    @Schema(title = "app page list")
    private List<AppPage> pages;

    @Schema(title = "apis, datasource in the apps")
    private List<SchemaRegistry> registries;
}
