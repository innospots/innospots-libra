package io.innospots.approve.core.model;

import io.innospots.approve.core.enums.ApproveStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

/**
 * approve submit form
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
@Getter
@Setter
@Schema(title = "approve submit form")
public class ApproveForm {

    @Schema(title = "approve instance key")
    private String approveInstanceKey;

    @Schema(title = "flow key")
    private String flowKey;

    @Schema(title = "app key")
    private String appKey;

    @Schema(title = "belong to project")
    private String belongTo;

    @Schema(title = "approve type")
    private String approveType;

    @Schema(title = "form data")
    private Map<String,Object> formData;

}
