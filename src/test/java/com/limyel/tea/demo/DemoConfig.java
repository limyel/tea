package com.limyel.tea.demo;

import com.limyel.tea.demo.bean.Cat;
import com.limyel.tea.demo.bean.Person;
import com.limyel.tea.ioc.annotation.Bean;
import com.limyel.tea.ioc.annotation.Config;
import com.sun.source.tree.CatchTree;

@Config
public class DemoConfig {

    @Bean
    public Cat cat() {
        Cat cat = new Cat();
        cat.setName("catea");
        return cat;
    }

}
