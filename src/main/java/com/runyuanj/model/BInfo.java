package com.runyuanj.model;

public class BInfo {

    private String name;

    public BInfo() {
        this.name = "B";
    }

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
