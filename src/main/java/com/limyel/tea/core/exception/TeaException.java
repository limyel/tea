package com.limyel.tea.core.exception;

public class TeaException extends RuntimeException {

    public TeaException() {
    }

    public TeaException(String msg) {
        super(msg);
    }
    public TeaException(Throwable cause) {
        super(cause);
    }

    public TeaException(String msg, Throwable cause) {
        super(msg, cause);
    }

}
