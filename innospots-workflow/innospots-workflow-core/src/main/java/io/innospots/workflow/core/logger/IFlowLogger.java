package io.innospots.workflow.core.logger;

import cn.hutool.core.util.StrUtil;
import io.innospots.base.execution.ExecutionResource;
import io.innospots.base.utils.CCH;

import java.util.Map;

/**
 * @author Smars
 * @date 2024/6/26
 */
public interface IFlowLogger {

    void item(String flowExecution,String nodeExecutionId, Map<String,Object> item);

    void resource(String flowExecution, String nodeExecutionId, ExecutionResource resource);

    void flowInfo(String sessionId,Object message);

    void flowError(String sessionId,Object message);

    void nodeInfo(String sessionId,Object message);

    void nodeError(String sessionId,Object message);

    default void flowInfo(Object message){
        flowInfo(CCH.contextInfo(),message);
    }

    default void flowError(Object message){
        flowError(CCH.contextInfo(),message);
    }

    default void nodeInfo(Object message){
        nodeInfo(CCH.contextInfo(),message);
    }

    default void nodeError(Object message){
        nodeError(CCH.contextInfo(),message);
    }

    default void flowInfo(String format,Object... message){
        flowInfo(CCH.contextInfo(), StrUtil.format(format,message));
    }

    default void flowError(String format,Object... message){
        flowError(CCH.contextInfo(),StrUtil.format(format,message));
    }

    default void nodeInfo(String format,Object... message){
        nodeInfo(CCH.contextInfo(),StrUtil.format(format,message));
    }

    default void nodeError(String format,Object... message){
        nodeError(CCH.contextInfo(),StrUtil.format(format,message));
    }

}
