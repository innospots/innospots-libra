package io.innospots.app.core.model.form;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
@Getter
@Setter
@Schema(title = "app create form")
public class CreateAppFrom {

    @Schema(title = "name")
    private String name;

    @Schema(title = "category id")
    private Integer categoryId;

    @Schema(title = "icon")
    private String icon;
}
