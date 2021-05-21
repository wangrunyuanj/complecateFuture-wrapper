package com.runyuanj.model;

public class AInfo {

    private String name;

    public String getName() {
        return this.name;
    }

    public void say() {
        System.out.println("This is " + this.name);
    }

    public AInfo() {
        this.name = "A";
    }
}
