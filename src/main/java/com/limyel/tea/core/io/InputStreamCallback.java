package com.limyel.tea.core.io;

import java.io.IOException;
import java.io.InputStream;

/**
 * 输入流回调函数
 *
 * @param <T>
 */
public interface InputStreamCallback<T> {

    T doWithInputStream(InputStream is) throws IOException;

}
