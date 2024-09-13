package io.innospots.workflow.core.runtime.webhook;

import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.execution.model.node.OutputDisplay;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @date 2024/9/13
 */
@Getter
@Setter
@Schema(title = "App Response Body")
public class AppResponseBody {

    private OutputDisplay outputs;
    @Schema(title = "output data schema fields")
    protected List<ParamField> schemaFields;

    @Schema(title = "output log")
    private Map<String, Object> logs = new LinkedHashMap<>();
}
