package com.runyuanj.model;

public class DInfo {

    private String name;

    public DInfo() {
        this.name = "D";
    }

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }
}
