package io.innospots.workflow.core.node.definition.meta;

import io.innospots.base.condition.Factor;
import io.innospots.base.condition.Relation;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/6/11
 */
@Getter
@Setter
public class CompCondition {

    private String result;

    private Relation relation;

    private List<Factor> children;
}
