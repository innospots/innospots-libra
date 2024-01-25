package io.innospots.app.core.model.form;

import io.innospots.base.enums.DataStatus;
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
@Schema(title = "app query request")
public class AppQueryRequest {

    @Schema(title = "data status")
    private DataStatus dataStatus;

    @Schema(title = "category Id")
    protected Integer categoryId;

    @Schema(title = "query input something")
    protected String queryInput;

    @Schema(title = "the current number of pages")
    protected Integer page = 1;

    @Schema(title = "number of entries per page")
    protected Integer size = 20;
}
