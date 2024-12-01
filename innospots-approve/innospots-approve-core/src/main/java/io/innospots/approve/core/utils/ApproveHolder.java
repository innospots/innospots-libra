package io.innospots.approve.core.utils;

import io.innospots.approve.core.model.ApproveActor;
import io.innospots.approve.core.model.ApproveFlowInstance;
import io.innospots.base.model.user.UserInfo;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
public class ApproveHolder {

    private static final ThreadLocal<ApproveFlowInstance> INSTANCE = new ThreadLocal<>();
    private static final ThreadLocal<UserInfo> LOGIN_USER = new ThreadLocal<>();
    private static final ThreadLocal<ApproveActor> ACTOR = new ThreadLocal<>();

    public static void set(ApproveFlowInstance approveFlowInstance) {
        INSTANCE.set(approveFlowInstance);
    }

    public static ApproveFlowInstance get() {
        return INSTANCE.get();
    }

    public static void remove() {
        INSTANCE.remove();
        ACTOR.remove();
        LOGIN_USER.remove();
    }

    public static void setLoginUser(UserInfo userInfo) {
        LOGIN_USER.set(userInfo);
    }

    public static UserInfo getLoginUser() {
        return LOGIN_USER.get();
    }

    public static void setActor(ApproveActor actor) {
        ACTOR.set(actor);
    }

    public static ApproveActor getActor() {
        return ACTOR.get();
    }
    public static void removeActor() {
        ACTOR.remove();
    }

}
