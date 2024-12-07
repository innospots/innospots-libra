package io.innospots.libra.kernel.module.system.model.user;

import io.innospots.base.model.user.UserSimpleGroup;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/7
 */
@Getter
@Setter
public class UserGroup extends UserSimpleGroup {

    private UserGroup parentGroup;

    private int userCount;
}
