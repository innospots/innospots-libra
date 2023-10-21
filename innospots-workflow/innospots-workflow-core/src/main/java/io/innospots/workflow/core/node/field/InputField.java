package io.innospots.workflow.core.node.field;

import io.innospots.base.condition.Factor;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2023/9/24
 */
@Getter
@Setter
public class InputField {

    protected Factor field;

    protected String code;

    protected String param;

    protected String value;
}
