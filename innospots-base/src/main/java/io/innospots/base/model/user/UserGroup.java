package io.innospots.base.model.user;

import jakarta.persistence.Column;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/4
 */
@Getter
@Setter
public class UserGroup {

    private Integer groupId;

    private String groupName;

    private String groupCode;

    private Integer parentGroupId;

    private Integer headUserId;

    private Integer[] assistantUserIds;

}
