package io.innospots.workflow.core.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/4
 */
@Getter
@Setter
@Builder
@Schema(description = "view card info")
public class ViewCard {

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String viewId;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String icon;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String title;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String src;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String description;

    private Object data;

    private CardViewType viewType;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String downloadUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String previewUrl;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String createTime;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userName;

    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String userId;

}
