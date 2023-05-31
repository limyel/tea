package com.limyel.tea.aop.exception;

import com.limyel.tea.core.exception.TeaException;

public class AopException extends TeaException {

    public AopException() {
    }

    public AopException(String msg) {
        super(msg);
    }
    public AopException(Throwable cause) {
        super(cause);
    }

    public AopException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
