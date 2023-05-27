package com.limyel.tea.web.util;

import com.limyel.tea.web.exception.WebException;

import java.util.regex.Pattern;

public class PathUtil {

    public static Pattern compile(String path) {
        String regPath = path.replaceAll("\\{([a-zA-Z][a-zA-Z0-9]*)\\}", "(?<$1>[^/]*)");
        if (regPath.indexOf('{') >= 0 || regPath.indexOf('}') > 0) {
            throw new WebException("invalid path: " + path);
        }
        return Pattern.compile("^" + regPath + "$");
    }

}
