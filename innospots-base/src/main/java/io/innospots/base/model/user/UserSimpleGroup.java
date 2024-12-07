package io.innospots.base.model.user;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/4
 */
@Getter
@Setter
@Schema(title = "User Group")
public class UserSimpleGroup {

    protected Integer groupId;

    protected String groupName;

    protected String groupCode;

    protected Integer parentGroupId;

    protected Integer headUserId;

    protected List<Integer> assistantUserIds;

}
