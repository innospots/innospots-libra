package io.innospots.workflow.core.runtime;

import io.innospots.base.events.EventBody;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @date 2023/9/19
 */
@Getter
@Setter
public class FlowEventBody extends EventBody {

    private String flowKey;

    private String nodeCode;

}
