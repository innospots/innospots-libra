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
public class NodeSetting {

    @Schema(title = "node css style")
    private Map<String, Object> style;

    @Schema(title = "node icon color")
    private String color;

    @Schema(title = "the node running method when click the execution button")
    private RunMethod runMethod;

    @Schema(title = "node width")
    private int width=150;

    @Schema(title = "node height")
    private int height=150;

    public enum RunMethod {
        /**
         *
         */
        DEFAULT,
        CONFIG;
    }

}
