package com.limyel.tea.demo;

import com.limyel.tea.boot.TeaApplication;
import com.limyel.tea.ioc.annotation.Config;

@Config
public class Main {

    public static void main(String[] args) {
        TeaApplication.run(Main.class, args);
    }
}
