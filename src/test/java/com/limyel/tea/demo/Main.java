package com.limyel.tea.demo;

import com.limyel.tea.demo.bean.Cat;
import com.limyel.tea.demo.bean.Person;
import com.limyel.tea.ioc.bean.container.BeanContainer;
import com.limyel.tea.ioc.bean.container.DefaultBeanContainer;

public class Main {
    public static void main(String[] args) {
        BeanContainer beanContainer = new DefaultBeanContainer(DemoConfig.class);
        Person person = (Person) beanContainer.getBean("person");
        Cat cat = beanContainer.getBean(Cat.class);
        System.out.println(person.getName());
        System.out.println(person.getAge());
        System.out.println(cat.getName());
    }
}
