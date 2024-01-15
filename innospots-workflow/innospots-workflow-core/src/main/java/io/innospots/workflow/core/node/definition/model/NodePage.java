package io.innospots.workflow.core.node.definition.model;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@Getter
@Setter
public class NodePage {

    @Schema(title = "page primary key")
    private String pageKey;

    @Schema(title = "form element components")
    private Map<String,Object> components;
}
