package io.innospots.app.console.form;

import io.innospots.base.enums.DataStatus;
import io.swagger.v3.oas.annotations.media.Schema;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/1/15
 */
public class AppQueryRequest {

    private DataStatus dataStatus;

    protected Integer categoryId;

    @Schema(title = "query input something")
    protected String queryInput;

    @Schema(title = "the current number of pages")
    protected int page = 1;

    @Schema(title = "number of entries per page")
    protected int size = 20;
}
