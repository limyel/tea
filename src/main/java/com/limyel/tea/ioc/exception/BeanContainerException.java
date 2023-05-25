package com.limyel.tea.ioc.exception;

import com.limyel.tea.core.exception.TeaException;

public class BeanContainerException extends TeaException {

    public BeanContainerException() {
    }

    public BeanContainerException(String msg) {
        super(msg);
    }
    public BeanContainerException(Throwable cause) {
        super(cause);
    }

    public BeanContainerException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
