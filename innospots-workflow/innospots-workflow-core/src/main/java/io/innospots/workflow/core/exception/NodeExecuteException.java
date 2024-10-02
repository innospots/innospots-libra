package io.innospots.workflow.core.exception;

import io.innospots.base.exception.BaseException;
import io.innospots.base.model.response.ResponseCode;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/10/2
 */
public class NodeExecuteException extends BaseException {
    public NodeExecuteException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }

    public static NodeExecuteException buildException(Class<?> invokeClass, Throwable cause, Object... params) {
        return new NodeExecuteException(invokeClass, ResponseCode.EXECUTE_ERROR, cause,params);
    }

    public static NodeExecuteException buildException(Class<?> invokeClass, Object... params) {
        return new NodeExecuteException(invokeClass, ResponseCode.EXECUTE_ERROR, null,params);
    }
}
