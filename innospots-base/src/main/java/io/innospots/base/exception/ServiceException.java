package io.innospots.base.exception;

import io.innospots.base.model.response.ResponseCode;

/**
 * @author Smars
 * @vesion 2.0
 * @date 2024/12/5
 */
public class ServiceException extends BaseException {

    public ServiceException(Class<?> invokeClass, String code, String detail, Throwable cause, Object... params) {
        super(invokeClass, code, detail, cause, params);
    }

    public ServiceException(String module, String code, String detail, Object... params) {
        super(module, code, detail, params);
    }

    public ServiceException(Class<?> invokeClass, ResponseCode responseCode, String message, String detail) {
        super(invokeClass, responseCode, message, detail);
    }

    public ServiceException(Class<?> invokeClass, ResponseCode responseCode, Throwable cause, Object... params) {
        super(invokeClass, responseCode, cause, params);
    }
}
