package com.limyel.tea.demo.bean;

import com.limyel.tea.ioc.annotation.Autowired;
import com.limyel.tea.ioc.annotation.Component;
import com.limyel.tea.ioc.annotation.Value;

@Component
public class Person {

    @Value("${person.name}")
    private String name;

    @Value("${person.age}")
    private Integer age;

    @Autowired
    private Cat cat;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public Cat getCat() {
        return cat;
    }

    public void setCat(Cat cat) {
        this.cat = cat;
    }
}
