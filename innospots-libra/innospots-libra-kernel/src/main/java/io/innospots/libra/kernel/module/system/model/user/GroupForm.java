package io.innospots.libra.kernel.module.system.model.user;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/7
 */
@Getter
@Setter
public class GroupForm {

    @NotNull
    protected String groupName;

    protected String groupCode;

    protected Integer parentGroupId;

    protected Integer headUserId;
}
