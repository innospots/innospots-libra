package io.innospots.base.service;

import io.innospots.base.model.user.UserGroup;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/4
 */
public interface IUserGroupService {

    UserGroup findUserGroupByUserId(Integer userId);

    UserGroup findParentUserGroupByUserId(Integer userId);

    /**
     *
     * @param userId
     * @param level 0: current group, 1: parent group, 2: grandparent group, 3: great-grandparent group etc.
     * @return
     */
    UserGroup findParentUserGroupByUserId(Integer userId,Integer level);
}
