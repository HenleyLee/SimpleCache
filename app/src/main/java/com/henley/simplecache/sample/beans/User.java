package com.henley.simplecache.sample.beans;

import android.support.annotation.NonNull;

import java.io.Serializable;

public class User implements Serializable {

    private String name;
    private String age;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAge() {
        return age;
    }

    public void setAge(String age) {
        this.age = age;
    }

    @NonNull
    @Override
    public String toString() {
        return "User [name=" + name + ", age=" + age + "]";
    }

}
