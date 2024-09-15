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

    void item(String flowExecutionId, Map<String, Object> item);

    void resource(String flowExecutionId, ExecutionResource resource);

    void flowInfo(String flowExecutionId, Object message);

    void flowError(String flowExecutionId, Object message);

    default void flowInfo(Object message) {
        flowInfo(CCH.sessionId(), message);
    }

    default void flowError(Object message) {
        flowError(CCH.sessionId(), message);
    }

    default void flowInfo(String format, Object... message) {
        flowInfo(CCH.sessionId(), StrUtil.format(format, message));
    }

    default void flowError(String format, Object... message) {
        flowError(CCH.sessionId(), StrUtil.format(format, message));
    }

    default void nodeStatus(String status, String nodeKey, String nodeName) {
        flowInfo(CCH.sessionId(), "status: " + status + ", nodeKey: " + nodeKey + ", name: " + nodeName);
    }

    default void nodeInfo(String nodeKey, String nodeName, String format, Object... message) {
        flowInfo(CCH.sessionId(), "nodeKey: " + nodeKey + ", name: " + nodeName + ", " + StrUtil.format(format, message));
    }

    default void nodeError(String nodeKey, String nodeName, String format, Object... message) {
        flowError(CCH.sessionId(), "nodeKey: " + nodeKey + ", name: " + nodeName + ", " + StrUtil.format(format, message));
    }

    default void flowInfoById(String flowExecutionId, String format, Object... message) {
        flowInfo(flowExecutionId, StrUtil.format(format, message));
    }

    default void flowErrorById(String flowExecutionId, String format, Object... message) {
        flowError(flowExecutionId, StrUtil.format(format, message));
    }


}
