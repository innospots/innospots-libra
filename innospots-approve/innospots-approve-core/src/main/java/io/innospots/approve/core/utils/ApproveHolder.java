package io.innospots.approve.core.utils;

import io.innospots.approve.core.model.ApproveFlowInstance;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/11/23
 */
public class ApproveHolder {

    private static final ThreadLocal<ApproveFlowInstance> INSTANCE = new ThreadLocal<>();

    public static void set(ApproveFlowInstance approveFlowInstance){
        INSTANCE.set(approveFlowInstance);
    }

    public static ApproveFlowInstance get(){
        return INSTANCE.get();
    }

    public static void remove(){
        INSTANCE.remove();
    }
}
