package com.limyel.tea.web.exception;

import com.limyel.tea.core.exception.TeaException;

public class WebException extends TeaException {

    public WebException() {
    }

    public WebException(String msg) {
        super(msg);
    }
    public WebException(Throwable cause) {
        super(cause);
    }

    public WebException(String msg, Throwable cause) {
        super(msg, cause);
    }
    
}
