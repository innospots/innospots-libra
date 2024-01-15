package io.innospots.workflow.core.node.definition.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@Getter
@Setter
public class NodeResource {

    @Schema(title = "node pages")
    private List<NodePage> pages;

    @Schema(title = "input ports, only use in frontend which define the input ports")
    private List<Map<String, Object>> inPorts;

    @Schema(title = "output ports, only use in frontend which define the output ports")
    private List<Map<String, Object>> outPorts;
}
