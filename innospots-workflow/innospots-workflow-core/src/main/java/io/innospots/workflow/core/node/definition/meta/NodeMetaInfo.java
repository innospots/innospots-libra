package io.innospots.workflow.core.node.definition.meta;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.innospots.base.model.field.ParamField;
import io.innospots.workflow.core.enums.NodePrimitive;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Map;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
@Getter
@Setter
public class NodeMetaInfo {

    @JsonIgnore
    private String metaFile;

    @NotNull(message = "node name cannot be empty")
    @Schema(title = "node display name")
    protected String name;

    @NotNull(message = "node code cannot be empty")
    @Schema(title = "node code")
    protected String code;

    @NotNull(message = "flow template code cannot be empty")
    @Schema(title = "flow template code")
    protected String flowCode;

    @NotNull(message = "node primitive type cannot be empty")
    @Schema(title = "node primitive type")
    protected NodePrimitive primitive;

    @Schema(title = "node icon image base64")
    protected String icon;

    @Schema(title = "node description")
    protected String description;

    @Schema(title = "node vendor")
    protected String vendor;

    @Schema(title = "node class name")
    protected String nodeType;

    @Schema(title = "credential type code")
    protected String credentialTypeCode;

    @Schema(title = "node setting")
    protected Map<String,Object> settings;

    @Schema(title = "node ports")
    protected Object outPorts;

    @Schema(title = "node ports")
    protected Object inPorts;

    @Schema(title = "node components")
    protected List<NodeComponent> components;

    @Schema(title = "node outputs")
    protected List<ParamField> outputs;

    @Schema(title = "execute script code")
    protected Map<String,Object> scripts;

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("{");
        sb.append("name='").append(name).append('\'');
        sb.append(", code='").append(code).append('\'');
        sb.append(", primitive=").append(primitive);
        sb.append(", description='").append(description).append('\'');
        sb.append(", vendor='").append(vendor).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
