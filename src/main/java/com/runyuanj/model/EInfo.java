package com.runyuanj.model;

public class EInfo {

    private String name;

    public EInfo() {
        this.name = "E";
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
