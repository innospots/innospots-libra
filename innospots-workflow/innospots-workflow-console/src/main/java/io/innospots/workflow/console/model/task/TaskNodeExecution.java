package io.innospots.workflow.console.model.task;

import io.innospots.workflow.core.execution.node.NodeExecutionBase;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2023/7/17
 */
@Getter
@Setter
@Schema(title = "")
public class TaskNodeExecution extends NodeExecutionBase {

    @Schema(title = "app node definition name")
    private String nodeName;

    @Schema(title = "execution mode: manual or trigger")
    private String triggerMode;
}
