package com.limyel.tea.ioc.exception;

import com.limyel.tea.core.exception.TeaException;

public class BeanDefineException extends TeaException {

    public BeanDefineException() {
    }

    public BeanDefineException(String msg) {
        super(msg);
    }
    public BeanDefineException(Throwable cause) {
        super(cause);
    }

    public BeanDefineException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
