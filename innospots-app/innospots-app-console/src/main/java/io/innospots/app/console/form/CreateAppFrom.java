package io.innospots.app.console.form;

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

    private String name;

    private Integer categoryId;

    private String icon;
}
