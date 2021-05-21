package com.runyuanj.model;

public class EInfo {

    private String name;

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }

    public EInfo() {
        this.name = "E";
    }
}
