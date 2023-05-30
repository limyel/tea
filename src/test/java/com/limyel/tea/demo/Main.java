package com.limyel.tea.demo;

import com.limyel.tea.boot.Tea;
import com.limyel.tea.boot.annotation.TeaApplication;
import com.limyel.tea.ioc.annotation.Config;

@TeaApplication
public class Main {

    public static void main(String[] args) {
        Tea.run(Main.class, args);
    }
}
