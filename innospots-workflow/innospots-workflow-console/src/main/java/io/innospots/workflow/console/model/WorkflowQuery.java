package io.innospots.workflow.console.model;

import io.innospots.base.data.request.FormQuery;
import io.innospots.base.enums.DataStatus;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class WorkflowQuery extends FormQuery {

    @Schema(title = "node primitive")
    private NodePrimitive primitive;

    @Schema(title = "data status")
    private DataStatus dataStatus;

    private String flowCode;
}
